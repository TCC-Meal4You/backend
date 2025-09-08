package com.api.meal4you.service;

import com.api.meal4you.entity.Usuario;
import com.api.meal4you.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public void cadastrarUsuario(Usuario usuario) {
        usuarioRepository.saveAndFlush(usuario); // Lembrar de criptografar!
    }

    public Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
    }

    public void deletarUsuarioPorEmail(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
        if (!usuario.getSenha().equals(senha)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta");
        }
        usuarioRepository.deleteByEmail(email);
    }

    @Transactional
    public void atualizarUsuarioPorId(int id, Usuario usuario) {
        Usuario usuarioEntity = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        boolean alterado = false;

        if (usuario.getNome() != null && !usuario.getNome().isBlank()
                && !usuario.getNome().equals(usuarioEntity.getNome())) {
            usuarioEntity.setNome(usuario.getNome());
            alterado = true;
        }

        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()
                && !usuario.getEmail().equals(usuarioEntity.getEmail())) {
            usuarioEntity.setEmail(usuario.getEmail());
            alterado = true;
        }

        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()
                && !usuario.getSenha().equals(usuarioEntity.getSenha())) {
            usuarioEntity.setSenha(usuario.getSenha()); // Lembrar de criptografar!
            alterado = true;
        }

        if (usuario.getLocalizacao() != null && !usuario.getLocalizacao().isBlank()
                && !usuario.getLocalizacao().equals(usuarioEntity.getLocalizacao())) {
            usuarioEntity.setLocalizacao(usuario.getLocalizacao());
            alterado = true;
        }

        if (usuario.getData_nascimento() != null
                && !usuario.getData_nascimento().equals(usuarioEntity.getData_nascimento())) {
            usuarioEntity.setData_nascimento(usuario.getData_nascimento());
            alterado = true;
        }

        if (usuario.getTempo_disponivel() != null
                && !usuario.getTempo_disponivel().equals(usuarioEntity.getTempo_disponivel())) {
            usuarioEntity.setTempo_disponivel(usuario.getTempo_disponivel());
            alterado = true;
        }

        if (!alterado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
        }

        usuarioRepository.save(usuarioEntity);
    }
}