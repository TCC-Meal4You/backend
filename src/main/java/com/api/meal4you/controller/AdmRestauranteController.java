package com.api.meal4you.controller;

import com.api.meal4you.dto.AdmRestauranteRequestDTO;
import com.api.meal4you.dto.AdmRestauranteResponseDTO;
import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.service.AdmRestauranteService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdmRestauranteController {

    private final AdmRestauranteService admRestauranteService;

    @PostMapping
    public ResponseEntity<AdmRestauranteResponseDTO> cadastrarAdm(@RequestBody AdmRestauranteRequestDTO dto) {
        AdmRestauranteResponseDTO response = admRestauranteService.cadastrarAdm(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<AdmRestauranteResponseDTO> buscarMeuPerfil() {
        AdmRestauranteResponseDTO response = admRestauranteService.buscarMeuPerfil();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<AdmRestauranteResponseDTO> atualizarMeuPerfil(@RequestBody AdmRestauranteRequestDTO dto) {
        AdmRestauranteResponseDTO response = admRestauranteService.atualizarMeuPerfil(dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deletarMinhaConta(@RequestParam String senha) {
        admRestauranteService.deletarMinhaConta(senha);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = admRestauranteService.fazerLogin(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String header) {
        admRestauranteService.logout(header);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-global")
    public ResponseEntity<Void> logoutGlobal() {
        admRestauranteService.logoutGlobal();
        return ResponseEntity.ok().build();
    }
}
