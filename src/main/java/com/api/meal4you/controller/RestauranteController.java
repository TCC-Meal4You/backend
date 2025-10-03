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

    @PostMapping("/cadastrar/{idAdmin}")  //Para cadastrar, pegar id do adm armazenado no front, ao fazer login (adm logado)
    public ResponseEntity<RestauranteResponseDTO> cadastrarRestaurante(@RequestBody RestauranteRequestDTO dto, @PathVariable Integer idAdmin) {
        RestauranteResponseDTO response = restauranteService.cadastrarRestaurante(dto, idAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<RestauranteResponseDTO>> listarRestaurantes(){
        return ResponseEntity.ok(restauranteService.listarTodos());
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<RestauranteResponseDTO> atualizarRestaurantePorId(@RequestParam int id, @RequestBody RestauranteRequestDTO dto) {
        RestauranteResponseDTO response = restauranteService.atualizarPorId(id, dto);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/deletar")
    public ResponseEntity<Void> excluirRestaurantes(@RequestParam String nome, @RequestParam String localizacao) {
        restauranteService.deletarRestaurante(nome, localizacao);
        return ResponseEntity.ok().build();
    }
}
