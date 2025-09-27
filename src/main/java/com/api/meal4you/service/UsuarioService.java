package com.api.meal4you.service;

import com.api.meal4you.dto.UsuarioRequestDTO;
import com.api.meal4you.dto.UsuarioResponseDTO;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.mapper.UsuarioMapper;
import com.api.meal4you.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;

    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto) {
        Usuario usuario = UsuarioMapper.toEntity(dto);

        usuario.setSenha(encoder.encode(usuario.getSenha()));

        usuarioRepository.saveAndFlush(usuario);
        return UsuarioMapper.toResponse(usuario);
    }

    public UsuarioResponseDTO buscarUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
        return UsuarioMapper.toResponse(usuario);
    }

    public void deletarUsuarioPorEmail(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));

        if (!encoder.matches(senha, usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta");
        }
        usuarioRepository.deleteByEmail(email);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuarioPorId(int id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        boolean alterado = false;

        if (dto.getNome() != null && !dto.getNome().isBlank()
                && !dto.getNome().equals(usuario.getNome())) {
            usuario.setNome(dto.getNome());
            alterado = true;
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && !dto.getEmail().equals(usuario.getEmail())) {
            usuario.setEmail(dto.getEmail());
            alterado = true;
        }

        if (dto.getSenha() != null && !dto.getSenha().isBlank()
                && !encoder.matches(dto.getSenha(), usuario.getSenha())) {
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreto");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", usuario.getId_usuario());
        response.put("nome", usuario.getNome());
        response.put("email", usuario.getEmail());

        return response;
    }
}
