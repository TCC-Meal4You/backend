package com.api.meal4you.controller;

import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.service.RestauranteService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/restaurante")
@RequiredArgsConstructor
public class RestauranteController {

    private final RestauranteService restauranteService;

    @PostMapping("/cadastrar")
    public ResponseEntity<RestauranteResponseDTO> cadastrarRestaurante(@RequestBody RestauranteRequestDTO dto) {
        RestauranteResponseDTO response = restauranteService.cadastrarRestaurante(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<RestauranteResponseDTO>> listarRestaurantes(){
        List<RestauranteResponseDTO> response = restauranteService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<RestauranteResponseDTO> atualizarRestaurantePorId(@PathVariable int id, @RequestBody RestauranteRequestDTO dto) {
        RestauranteResponseDTO response = restauranteService.atualizarPorAdmLogado(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deletar")
    public ResponseEntity<Void> excluirRestaurantes(@RequestParam String nome, @RequestParam String localizacao) {
        restauranteService.deletarRestaurante(nome, localizacao);
        return ResponseEntity.ok().build();
    }
}
