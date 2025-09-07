package com.api.meal4you.service;

import com.api.meal4you.entity.Usuario;
import com.api.meal4you.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public void cadastrarUsuario(Usuario usuario) {
        usuarioRepository.saveAndFlush(usuario);
    }

    public Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email não encontrado"));
    }

    public void deletarUsuarioPorEmail(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email não encontrado"));
        if (!usuario.getSenha().equals(senha)) {
            throw new RuntimeException("Senha incorreta");
        }
        usuarioRepository.deleteByEmail(email);
    }

    public void atualizarUsuarioPorId(int id, Usuario usuario) {
        if (usuario.getData_nascimento() == null) {
            throw new RuntimeException("Data de nascimento não pode ser nula ou vazia");
        }

        Usuario usuarioEntity = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario não encontrado"));

        Usuario usuarioAtualizado = Usuario.builder()
                .id_usuario(usuarioEntity.getId_usuario())
                .email(usuario.getEmail() != null ? usuario.getEmail() : usuarioEntity.getEmail())
                .nome(usuario.getNome() != null ? usuario.getNome() : usuarioEntity.getNome())
                .senha(usuario.getSenha() != null ? usuario.getSenha() : usuarioEntity.getSenha())
                .localizacao(usuario.getLocalizacao() != null ? usuario.getLocalizacao() : usuarioEntity.getLocalizacao())
                .data_nascimento(usuario.getData_nascimento())
                .tempo_disponivel(usuario.getTempo_disponivel() != null ? usuario.getTempo_disponivel() : usuarioEntity.getTempo_disponivel())
                .build();

        usuarioRepository.saveAndFlush(usuarioAtualizado);
    }
}
