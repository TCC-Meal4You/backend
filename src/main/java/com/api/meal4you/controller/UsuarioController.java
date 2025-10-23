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

import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.dto.UsuarioRequestDTO;
import com.api.meal4you.dto.UsuarioResponseDTO;
import com.api.meal4you.dto.UsuarioRestricaoRequestDTO;
import com.api.meal4you.dto.VerificaEmailRequestDTO;
import com.api.meal4you.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/verifica-email")
    public ResponseEntity<Map<String, String>> enviarCodigo(@Valid @RequestBody VerificaEmailRequestDTO dto) {
        usuarioService.enviarCodigoVerificacao(dto.getEmail());
        return ResponseEntity.ok(Map.of("mensagem", "Código de verificação enviado para o e-mail."));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrarUsuario(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<UsuarioResponseDTO> buscarMeuPerfil() {
        UsuarioResponseDTO response = usuarioService.buscarMeuPerfil();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<UsuarioResponseDTO> atualizarMeuPerfil(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.atualizarMeuPerfil(dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deletarMinhaConta(@RequestParam String senha) {
        usuarioService.deletarMinhaConta(senha);
        return ResponseEntity.ok(Map.of("mensagem", "Usuário deletado com sucesso."));
    }

    @PutMapping("/restricoes")
    public ResponseEntity<UsuarioResponseDTO> atualizarMinhasRestricoes(@RequestBody UsuarioRestricaoRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.atualizarMinhasRestricoes(dto);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = usuarioService.fazerLogin(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String header) {
        usuarioService.logout(header);
        return ResponseEntity.ok(Map.of("mensagem", "Logout realizado com sucesso."));
    }

    @PostMapping("/logout-global")
    public ResponseEntity<Map<String, String>> logoutGlobal() {
        usuarioService.logoutGlobal();
        return ResponseEntity.ok(Map.of("mensagem", "Logout global realizado com sucesso."));
    }
}
