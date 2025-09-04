package com.api.meal4you.service;

import com.api.meal4you.entity.Usuario;
import com.api.meal4you.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public void salvarUsuario(Usuario usuario) {
        repository.saveAndFlush(usuario);
    }

    public Usuario buscarUsuarioPorEmail(String email) {
        return repository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email não encontrado"));
    }

    public void deletarUsuarioPorEmail(String email, String senha) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email não encontrado"));
        if (!usuario.getSenha().equals(senha)) {
            throw new RuntimeException("Senha incorreta");
        }
        repository.deleteByEmail(email);
    }

    public void atualizarUsuarioPorId(int id, Usuario usuario) {
        Usuario usuarioEntity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario não encontrado"));
        Usuario usuarioAtualizado = Usuario.builder()
                .id(usuarioEntity.getId())
                .email(usuario.getEmail() != null ? usuario.getEmail() : usuarioEntity.getEmail())
                .nome(usuario.getNome() != null ? usuario.getNome() : usuarioEntity.getNome())
                .senha(usuario.getSenha() != null ? usuario.getSenha() : usuarioEntity.getSenha())
                .localizacao(
                        usuario.getLocalizacao() != null ? usuario.getLocalizacao() : usuarioEntity.getLocalizacao())
                .data_nascimento(usuario.getData_nascimento() != null ? usuario.getData_nascimento()
                        : usuarioEntity.getData_nascimento())
                .tempo_disponivel(usuario.getTempo_disponivel() != null ? usuario.getTempo_disponivel()
                        : usuarioEntity.getTempo_disponivel())
                .build();

        repository.saveAndFlush(usuarioAtualizado);
    }
}
