package com.api.meal4you.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.meal4you.dto.AdmRestauranteRequestDTO;
import com.api.meal4you.dto.AdmRestauranteResponseDTO;
import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.dto.VerificaEmailRequestDTO;
import com.api.meal4you.service.AdmRestauranteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdmRestauranteController {

    private final AdmRestauranteService admRestauranteService;

    @PostMapping("/verifica-email")
    public ResponseEntity<Map<String, String>> enviarCodigo(@Valid @RequestBody VerificaEmailRequestDTO dto) {
        admRestauranteService.enviarCodigoVerificacao(dto.getEmail());
        return ResponseEntity.ok(Map.of("mensagem", "Código de verificação enviado para o e-mail."));
    }

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
