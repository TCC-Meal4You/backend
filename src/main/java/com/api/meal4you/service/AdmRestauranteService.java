package com.api.meal4you.service;

import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.repository.AdmRestauranteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdmRestauranteService {
    private AdmRestauranteRepository repository;

    public AdmRestauranteService(AdmRestauranteRepository repository) {
        this.repository = repository;
    }

    public void salvarUsuario(AdmRestaurante admRestaurante) {
        repository.saveAndFlush(admRestaurante);
    }

    public Optional<AdmRestaurante> buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }

    public void atualizarPorId(int id, AdmRestaurante admRestaurante) {
        AdmRestaurante admEntity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrador de restaurante não encontrado"));

        AdmRestaurante admAtualizado = AdmRestaurante.builder()
                .id(admEntity.getId())
                .email(admRestaurante.getEmail() != null ? admRestaurante.getEmail() : admEntity.getEmail())
                .nome(admRestaurante.getNome() != null ? admRestaurante.getNome() : admEntity.getNome())
                .senha(admRestaurante.getSenha() != null ? admRestaurante.getSenha() : admEntity.getSenha())
                .build();

        repository.saveAndFlush(admAtualizado);
    }

    public void deletarPorEmail(String email, String senha) {
        AdmRestaurante admRestaurante = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email não encontrado"));
        if (!admRestaurante.getSenha().equals(senha)) {
            throw new RuntimeException("Senha incorreta");
        }
        repository.deleteByEmail(email);
    }
}
