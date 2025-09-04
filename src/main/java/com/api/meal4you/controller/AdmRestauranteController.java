package com.api.meal4you.controller;

import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.service.AdmRestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdmRestauranteController {

    private final AdmRestauranteService admRestauranteService;

    @PostMapping("/cadastrar")
    public ResponseEntity<Void> cadastrarUsuario(@RequestBody AdmRestaurante admRestaurante) {
        admRestauranteService.salvarUsuario(admRestaurante);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/email")
    public ResponseEntity<AdmRestaurante> buscarPorEmail(@RequestParam String email) {
        return admRestauranteService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarPorId(@PathVariable int id, @RequestBody AdmRestaurante admRestaurante) {
        admRestauranteService.atualizarPorId(id, admRestaurante);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/email")
    public ResponseEntity<Void> deletarUsuarioPorEmail(@RequestParam String email) {
        admRestauranteService.deletarPorEmail(email);
        return ResponseEntity.ok().build();
    }
}

