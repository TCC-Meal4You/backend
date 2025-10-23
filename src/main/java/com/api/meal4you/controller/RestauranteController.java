package com.api.meal4you.controller;

import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.service.RestauranteService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurantes")
@RequiredArgsConstructor
public class RestauranteController {

    private final RestauranteService restauranteService;

    @PostMapping
    public ResponseEntity<RestauranteResponseDTO> cadastrarRestaurante(@RequestBody RestauranteRequestDTO dto) {
        RestauranteResponseDTO response = restauranteService.cadastrarRestaurante(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RestauranteResponseDTO>> listarRestaurantes() {
        List<RestauranteResponseDTO> response = restauranteService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestauranteResponseDTO> atualizarRestaurantePorId(@PathVariable int id, @RequestBody RestauranteRequestDTO dto) {
        RestauranteResponseDTO response = restauranteService.atualizarPorAdmLogado(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deletarRestaurantes(@RequestParam String nome, @RequestParam String localizacao) {
        restauranteService.deletarRestaurante(nome, localizacao);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/meu-restaurante")
    public ResponseEntity<RestauranteResponseDTO> buscarMeuRestaurante() {
        RestauranteResponseDTO response = restauranteService.buscarMeuRestaurante();
        return ResponseEntity.ok(response);
    }
    
}
