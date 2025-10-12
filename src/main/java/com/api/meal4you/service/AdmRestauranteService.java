package com.api.meal4you.service;

import com.api.meal4you.dto.AdmRestauranteRequestDTO;
import com.api.meal4you.dto.AdmRestauranteResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.mapper.AdmRestauranteMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.security.JwtUtil;
import com.api.meal4you.security.TokenStore;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdmRestauranteService {
    private final AdmRestauranteRepository admRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;

    public String getAdmLogadoEmail() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado");
            }
            return authentication.getPrincipal().toString();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao obter administrador logado: " + ex.getMessage());
        }
    }

    private void validarAdmLogado(String email) {
        String emailLogado = getAdmLogadoEmail();
        if (!email.equals(emailLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode acessar outro administrador");
        }
    }

    public AdmRestauranteResponseDTO cadastrarAdm(AdmRestauranteRequestDTO dto) {
        try {
            // Verifica se o email já existe
            if (admRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
            }
            AdmRestaurante adm = AdmRestauranteMapper.toEntity(dto);
            adm.setSenha(encoder.encode(adm.getSenha()));
            admRepository.saveAndFlush(adm);
            return AdmRestauranteMapper.toResponse(adm);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao cadastrar administrador: " + ex.getMessage());
        }
    }

    public AdmRestauranteResponseDTO buscarPorEmail(String email) {
        try {
            validarAdmLogado(email);
            AdmRestaurante adm = admRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
            return AdmRestauranteMapper.toResponse(adm);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar administrador: " + ex.getMessage());
        }
    }

    @Transactional
    public AdmRestauranteResponseDTO atualizarPorId(int id, AdmRestauranteRequestDTO dto) {
        try {
            AdmRestaurante adm = admRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado"));

            validarAdmLogado(adm.getEmail());

            boolean alterado = false;

            if (dto.getNome() != null && !dto.getNome().isBlank() && !dto.getNome().equals(adm.getNome())) {
                adm.setNome(dto.getNome());
                alterado = true;
            }

            if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(adm.getEmail())) {
                tokenStore.removerTodosTokensDoUsuario(adm.getEmail());
                adm.setEmail(dto.getEmail());
                alterado = true;
            }

            if (dto.getSenha() != null && !dto.getSenha().isBlank() && !encoder.matches(dto.getSenha(), adm.getSenha())) {
                tokenStore.removerTodosTokensDoUsuario(adm.getEmail());
                adm.setSenha(encoder.encode(dto.getSenha()));
                alterado = true;
            }

            if (!alterado) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
            }

            admRepository.save(adm);
            return AdmRestauranteMapper.toResponse(adm);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar administrador: " + ex.getMessage());
        }
    }

    public void deletarPorEmail(String email, String senha) {
        try {
            validarAdmLogado(email);
            AdmRestaurante adm = admRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));

            if (!encoder.matches(senha, adm.getSenha())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta");
            }

            tokenStore.removerTodosTokensDoUsuario(adm.getEmail());
            admRepository.deleteByEmail(email);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao deletar administrador: " + ex.getMessage());
        }
    }

    public Map<String, Object> fazerLogin(String email, String senha) {
        try {
            AdmRestaurante adm = admRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta"));

            if (!encoder.matches(senha, adm.getSenha())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta");
            }

            String token = jwtUtil.gerarToken(adm.getEmail(), "ADMIN");
            tokenStore.salvarToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("id", adm.getId_admin());
            response.put("nome", adm.getNome());
            response.put("email", adm.getEmail());
            response.put("token", token);

            return response;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer login: " + ex.getMessage());
        }
    }

    public void logout(String header) {
        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                tokenStore.removerToken(token);
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer logout: " + ex.getMessage());
        }
    }

    public void logoutGlobal() {
        try {
            String emailLogado = getAdmLogadoEmail();
            tokenStore.removerTodosTokensDoUsuario(emailLogado);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer logout global: " + ex.getMessage());
        }
    }
}
