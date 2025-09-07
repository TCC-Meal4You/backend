package com.api.meal4you.controller;

import com.api.meal4you.entity.Restaurante;
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

    @PostMapping("/cadastrar/{idAdmin}")
    public ResponseEntity<Void> cadastrarRestaurante(@RequestBody Restaurante restaurante, @PathVariable Integer idAdmin) {
        restauranteService.cadastrarRestaurante(restaurante, idAdmin);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Restaurante>> listarRestaurantes(){
        return ResponseEntity.ok(restauranteService.listarTodos());
    }

    @PutMapping 
    public ResponseEntity<Void> atualizarRestaurantePorId(@RequestParam int id, @RequestBody Restaurante restaurante) {
        restauranteService.atualizarPorId(id, restaurante);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping
    public ResponseEntity<Void> excluirRestaurantes(@RequestParam String nome, @RequestParam String localizacao) {
        restauranteService.deletarRestaurante(nome, localizacao);
        return ResponseEntity.ok().build();
    }
}
