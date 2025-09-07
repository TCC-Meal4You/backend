package com.api.meal4you.service;

import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.repository.AdmRestauranteRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdmRestauranteService {
    private final AdmRestauranteRepository admRepository;

    public void cadastararAdm(AdmRestaurante admRestaurante) {
        admRepository.saveAndFlush(admRestaurante);
    }

    public Optional<AdmRestaurante> buscarPorEmail(String email) {
        return admRepository.findByEmail(email);
    }

    public void atualizarPorId(int id, AdmRestaurante admRestaurante) {
        AdmRestaurante admEntity = admRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrador de restaurante não encontrado"));

        AdmRestaurante admAtualizado = AdmRestaurante.builder()
                .id_admin(admEntity.getId_admin())
                .email(admRestaurante.getEmail() != null ? admRestaurante.getEmail() : admEntity.getEmail())
                .nome(admRestaurante.getNome() != null ? admRestaurante.getNome() : admEntity.getNome())
                .senha(admRestaurante.getSenha() != null ? admRestaurante.getSenha() : admEntity.getSenha())
                .build();

        admRepository.saveAndFlush(admAtualizado);
    }

    public void deletarPorEmail(String email, String senha) {
        AdmRestaurante admRestaurante = admRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email não encontrado"));
        if (!admRestaurante.getSenha().equals(senha)) {
            throw new RuntimeException("Senha incorreta");
        }
        admRepository.deleteByEmail(email);
    }
}
