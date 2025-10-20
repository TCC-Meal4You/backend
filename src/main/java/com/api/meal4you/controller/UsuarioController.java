package com.api.meal4you.controller;

import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.dto.UsuarioRequestDTO;
import com.api.meal4you.dto.UsuarioResponseDTO;
import com.api.meal4you.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrarUsuario(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<UsuarioResponseDTO> buscarMeuPefil() {
        UsuarioResponseDTO response = usuarioService.buscarMeuPerfil();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<UsuarioResponseDTO> atualizarMeuPefil(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.atualizarMeuPerfil(dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deletarMinhaConta(@RequestParam String senha) {
        usuarioService.deletarMinhaConta(senha);
        return ResponseEntity.ok(Map.of("mensagem", "Usuário deletado com sucesso."));
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
