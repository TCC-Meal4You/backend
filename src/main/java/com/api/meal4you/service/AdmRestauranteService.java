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
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void validarAdmLogado(String email) {
        String emailLogado = getAdmLogadoEmail();
        if (!email.equals(emailLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode acessar outro usuário");
        }
    }

    public AdmRestauranteResponseDTO cadastrarAdm(AdmRestauranteRequestDTO dto) {
        AdmRestaurante admRestaurante = AdmRestauranteMapper.toEntity(dto);

        admRestaurante.setSenha(encoder.encode(admRestaurante.getSenha()));

        admRepository.saveAndFlush(admRestaurante);
        return AdmRestauranteMapper.toResponse(admRestaurante);
    }

    public AdmRestauranteResponseDTO buscarPorEmail(String email) {
        validarAdmLogado(email);

        AdmRestaurante adm = admRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
        return AdmRestauranteMapper.toResponse(adm);
    }

    @Transactional
    public AdmRestauranteResponseDTO atualizarPorId(int id, AdmRestauranteRequestDTO dto) {
        AdmRestaurante adm = admRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Administrador de restaurante não encontrado"));
        
        validarAdmLogado(adm.getEmail());

        boolean alterado = false;

        if (dto.getNome() != null && !dto.getNome().isBlank()
                && !dto.getNome().equals(adm.getNome())) {
            adm.setNome(dto.getNome());
            alterado = true;
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && !dto.getEmail().equals(adm.getEmail())) {
            adm.setEmail(dto.getEmail());
            alterado = true;
        }

        if (dto.getSenha() != null && !dto.getSenha().isBlank()
                && !encoder.matches(dto.getSenha(), adm.getSenha())) {
            adm.setSenha(encoder.encode(dto.getSenha()));
            alterado = true;
        }

        if (!alterado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
        }

        admRepository.save(adm);
        return AdmRestauranteMapper.toResponse(adm);
    }

    public void deletarPorEmail(String email, String senha) {
        validarAdmLogado(email);

        AdmRestaurante admRestaurante = admRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
        if (!encoder.matches(senha, admRestaurante.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta");
        }
        admRepository.deleteByEmail(email);
    }

    public Map<String, Object> fazerLogin(String email, String senha) {
        AdmRestaurante admRestaurante = admRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email ou senha incorreta"));

        if (!encoder.matches(senha, admRestaurante.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta");
        }

        String token = jwtUtil.gerarToken(admRestaurante.getEmail(), "ADMIN");
        tokenStore.adicionarToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("id", admRestaurante.getId_admin());
        response.put("nome", admRestaurante.getNome());
        response.put("email", admRestaurante.getEmail());
        response.put("token", token);

        return response;
    }

    public void logout(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            tokenStore.removerToken(token);
        }
    }
}
