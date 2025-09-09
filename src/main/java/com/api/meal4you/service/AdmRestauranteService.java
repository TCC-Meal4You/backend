package com.api.meal4you.service;

import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.repository.AdmRestauranteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdmRestauranteService {
    private final AdmRestauranteRepository admRepository;

    public void cadastararAdm(AdmRestaurante admRestaurante) {
        admRepository.saveAndFlush(admRestaurante); // Lembra de cripitografar senha
    }

    public AdmRestaurante buscarPorEmail(String email) {
        return admRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
    }

    @Transactional
    public void atualizarPorId(int id, AdmRestaurante admRestaurante) {
        AdmRestaurante admEntity = admRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Administrador de restaurante não encontrado"));

        boolean alterado = false;

        if (admRestaurante.getNome() != null && !admRestaurante.getNome().isBlank()
                && !admRestaurante.getNome().equals(admEntity.getNome())) {
            admEntity.setNome(admRestaurante.getNome());
            alterado = true;
        }

        if (admRestaurante.getEmail() != null && !admRestaurante.getEmail().isBlank()
                && !admRestaurante.getEmail().equals(admEntity.getEmail())) {
            admEntity.setEmail(admRestaurante.getEmail());
            alterado = true;
        }

        if (admRestaurante.getSenha() != null && !admRestaurante.getSenha().isBlank()
                && !admRestaurante.getSenha().equals(admEntity.getSenha())) {
            admEntity.setSenha(admRestaurante.getSenha()); // Lembrar de criptografar senha!
            alterado = true;
        }

        if (!alterado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
        }

        admRepository.save(admEntity);
    }

    public void deletarPorEmail(String email, String senha) { //Lembra
        AdmRestaurante admRestaurante = admRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
        if (!admRestaurante.getSenha().equals(senha)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta");
        }
        admRepository.deleteByEmail(email);
    }

    public Map<String, Object> fazerLogin(String email, String senha) { 
        AdmRestaurante admRestaurante = admRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email ou senha incorreta"));

        if (!admRestaurante.getSenha().equals(senha)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", admRestaurante.getId_admin());
        response.put("nome", admRestaurante.getNome());
        response.put("email", admRestaurante.getEmail());

        return response;
    }
}
