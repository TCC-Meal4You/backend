package com.api.meal4you.service;

import com.api.meal4you.dto.AdmRestauranteRequestDTO;
import com.api.meal4you.dto.AdmRestauranteResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.mapper.AdmRestauranteMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;

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
public class AdmRestauranteService {
    private final AdmRestauranteRepository admRepository;
    private final PasswordEncoder encoder;

    public AdmRestauranteResponseDTO cadastrarAdm(AdmRestauranteRequestDTO dto) {
        AdmRestaurante admRestaurante = AdmRestauranteMapper.toEntity(dto);
        
        admRestaurante.setSenha(encoder.encode(admRestaurante.getSenha()));

        admRepository.saveAndFlush(admRestaurante);
        return AdmRestauranteMapper.toResponse(admRestaurante);
    }

    public AdmRestauranteResponseDTO buscarPorEmail(String email) {
        AdmRestaurante adm = admRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
        return AdmRestauranteMapper.toResponse(adm);
    }

    @Transactional
    public AdmRestauranteResponseDTO atualizarPorId(int id, AdmRestauranteRequestDTO dto) {
        AdmRestaurante admEntity = admRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Administrador de restaurante não encontrado"));

        boolean alterado = false;

        if (dto.getNome() != null && !dto.getNome().isBlank()
                && !dto.getNome().equals(admEntity.getNome())) {
            admEntity.setNome(dto.getNome());
            alterado = true;
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && !dto.getEmail().equals(admEntity.getEmail())) {
            admEntity.setEmail(dto.getEmail());
            alterado = true;
        }

        if (dto.getSenha() != null && !dto.getSenha().isBlank()
                && !encoder.matches(dto.getSenha(), admEntity.getSenha())) {
            admEntity.setSenha(encoder.encode(dto.getSenha()));
            alterado = true;
        }

        if (!alterado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
        }

        admRepository.save(admEntity);
        return AdmRestauranteMapper.toResponse(admEntity);
    }

    public void deletarPorEmail(String email, String senha) {
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

        Map<String, Object> response = new HashMap<>();
        response.put("id", admRestaurante.getId_admin());
        response.put("nome", admRestaurante.getNome());
        response.put("email", admRestaurante.getEmail());

        return response;
    }
}
