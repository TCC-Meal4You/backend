package com.api.meal4you.controller;

import com.api.meal4you.dto.AdmRestauranteRequestDTO;
import com.api.meal4you.dto.AdmRestauranteResponseDTO;
import com.api.meal4you.service.AdmRestauranteService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdmRestauranteController {

    private final AdmRestauranteService admRestauranteService;

    @PostMapping("/cadastrar")
    public ResponseEntity<AdmRestauranteResponseDTO> cadastrarAdm(@RequestBody AdmRestauranteRequestDTO dto) {
        AdmRestauranteResponseDTO response = admRestauranteService.cadastrarAdm(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar/email")
    public ResponseEntity<AdmRestauranteResponseDTO> buscarPorEmail(@RequestParam String email) {
        AdmRestauranteResponseDTO response = admRestauranteService.buscarPorEmail(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<AdmRestauranteResponseDTO> atualizarPorId(@PathVariable int id,
            @RequestBody AdmRestauranteRequestDTO dto) {
        AdmRestauranteResponseDTO response = admRestauranteService.atualizarPorId(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deletar/email")
    public ResponseEntity<Void> deletarAdmPorEmail(@RequestParam String email, String senha) {
        admRestauranteService.deletarPorEmail(email, senha);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AdmRestauranteRequestDTO dto) {
        Map<String, Object> response = admRestauranteService.fazerLogin(
                dto.getEmail(), dto.getSenha());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String header) {
        admRestauranteService.logout(header);
        return ResponseEntity.ok().build();
    }
}
