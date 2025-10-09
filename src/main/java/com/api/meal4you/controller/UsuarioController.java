package com.api.meal4you.controller;

import com.api.meal4you.dto.UsuarioRequestDTO;
import com.api.meal4you.dto.UsuarioResponseDTO;
import com.api.meal4you.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastrar")
    public ResponseEntity<UsuarioResponseDTO> cadastrarUsuario(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorEmail(@RequestParam String email) {
        UsuarioResponseDTO response = usuarioService.buscarUsuarioPorEmail(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/atualizar")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuarioPorId(@RequestParam int id, @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.atualizarUsuarioPorId(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deletar")
    public ResponseEntity<Map<String, String>> deletarUsuarioPorEmail(@RequestParam String email, @RequestParam String senha) {
        usuarioService.deletarUsuarioPorEmail(email, senha);
        return ResponseEntity.ok(Map.of("mensagem", "Usuário deletado com sucesso."));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UsuarioRequestDTO dto) {
        Map<String, Object> response = usuarioService.fazerLogin(dto.getEmail(), dto.getSenha());
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
