package com.api.meal4you.service;

import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.dto.UsuarioRequestDTO;
import com.api.meal4you.dto.UsuarioResponseDTO;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.mapper.LoginMapper;
import com.api.meal4you.mapper.UsuarioMapper;
import com.api.meal4you.repository.UsuarioRepository;
import com.api.meal4you.security.JwtUtil;
import com.api.meal4you.security.TokenStore;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;

    public String getUsuarioLogadoEmail() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
            }
            return authentication.getPrincipal().toString();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao obter usuário logado: " + ex.getMessage());
        }
    }

    private void validarUsuarioLogado(String email) {
        String emailLogado = getUsuarioLogadoEmail();
        if (!email.equals(emailLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode acessar outro usuário");
        }
    }

    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto) {
        try {
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            }
            Usuario usuario = UsuarioMapper.toEntity(dto);
            usuario.setSenha(encoder.encode(usuario.getSenha()));
            usuarioRepository.saveAndFlush(usuario);
            return UsuarioMapper.toResponse(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao cadastrar usuário: " + ex.getMessage());
        }
    }

    public UsuarioResponseDTO buscarUsuarioPorEmail(String email) {
        try {
            validarUsuarioLogado(email);
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
            return UsuarioMapper.toResponse(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar usuário: " + ex.getMessage());
        }
    }

    public void deletarUsuarioPorEmail(String email, String senha) {
        try {
            validarUsuarioLogado(email);
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));

            if (!encoder.matches(senha, usuario.getSenha())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta");
            }

            tokenStore.removerTodosTokensDoUsuario(usuario.getEmail());
            usuarioRepository.deleteByEmail(email);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao deletar usuário: " + ex.getMessage());
        }
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuarioPorId(int id, UsuarioRequestDTO dto) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

            validarUsuarioLogado(usuario.getEmail());
            boolean alterado = false;

            if (dto.getNome() != null && !dto.getNome().isBlank() && !dto.getNome().equals(usuario.getNome())) {
                usuario.setNome(dto.getNome());
                alterado = true;
            }
            if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(usuario.getEmail())) {
                tokenStore.removerTodosTokensDoUsuario(usuario.getEmail());
                usuario.setEmail(dto.getEmail());
                alterado = true;
            }
            if (dto.getSenha() != null && !dto.getSenha().isBlank()
                    && !encoder.matches(dto.getSenha(), usuario.getSenha())) {
                tokenStore.removerTodosTokensDoUsuario(usuario.getEmail());
                usuario.setSenha(encoder.encode(dto.getSenha()));
                alterado = true;
            }

            if (!alterado) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
            }

            usuarioRepository.save(usuario);
            return UsuarioMapper.toResponse(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar usuário: " + ex.getMessage());
        }
    }

    public LoginResponseDTO fazerLogin(LoginRequestDTO dto) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta"));

            if (!encoder.matches(dto.getSenha(), usuario.getSenha())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta");
            }

            String token = jwtUtil.gerarToken(usuario.getEmail(), "USUARIO");
            tokenStore.salvarToken(token);

            return LoginMapper.toResponse(usuario, token);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer login: " + ex.getMessage());
        }
    }

    public void logout(String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou ausente");
            }
            String token = header.substring(7);
            tokenStore.removerToken(token);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer logout: " + ex.getMessage());
        }
    }

    public void logoutGlobal() {
        try {
            String emailLogado = getUsuarioLogadoEmail();
            tokenStore.removerTodosTokensDoUsuario(emailLogado);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer logout global: " + ex.getMessage());
        }
    }
}
