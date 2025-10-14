package com.api.meal4you.service;

import com.api.meal4you.dto.UsuarioRequestDTO;
import com.api.meal4you.dto.UsuarioResponseDTO;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.mapper.UsuarioMapper;
import com.api.meal4you.repository.UsuarioRepository;
import com.api.meal4you.security.JwtUtil;
import com.api.meal4you.security.TokenStore;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;

    public String getUsuarioLogadoEmail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return authentication.getPrincipal().toString();
    }

    private void validarUsuarioLogado(String email) {
        String emailLogado = getUsuarioLogadoEmail();
        if (!email.equals(emailLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode acessar outro usuário");
        }
    }

    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        Usuario usuario = UsuarioMapper.toEntity(dto);
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        usuarioRepository.saveAndFlush(usuario);
        return UsuarioMapper.toResponse(usuario);
    }

    public UsuarioResponseDTO buscarUsuarioPorEmail(String email) {
        validarUsuarioLogado(email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
        return UsuarioMapper.toResponse(usuario);
    }

    public void deletarUsuarioPorEmail(String email, String senha) {
        validarUsuarioLogado(email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));

        if (!encoder.matches(senha, usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta");
        }

        tokenStore.removerTodosTokensDoUsuario(usuario.getEmail());
        usuarioRepository.deleteByEmail(email);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuarioPorId(int id, UsuarioRequestDTO dto) {
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
        if (dto.getSenha() != null && !dto.getSenha().isBlank() && !encoder.matches(dto.getSenha(), usuario.getSenha())) {
            tokenStore.removerTodosTokensDoUsuario(usuario.getEmail());
            usuario.setSenha(encoder.encode(dto.getSenha()));
            alterado = true;
        }

        if (!alterado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
        }

        usuarioRepository.save(usuario);
        return UsuarioMapper.toResponse(usuario);
    }

    public Map<String, Object> fazerLogin(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta"));

        if (!encoder.matches(senha, usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta");
        }

        String token = jwtUtil.gerarToken(usuario.getEmail(), "USUARIO");
        tokenStore.salvarToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("id", usuario.getIdUsuario());
        response.put("nome", usuario.getNome());
        response.put("email", usuario.getEmail());
        response.put("token", token);
        return response;
    }

    public void logout(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou ausente");
        }
        String token = header.substring(7);
        tokenStore.removerToken(token);
    }

    public void logoutGlobal() {
        String emailLogado = getUsuarioLogadoEmail();
        tokenStore.removerTodosTokensDoUsuario(emailLogado);
    }
}
