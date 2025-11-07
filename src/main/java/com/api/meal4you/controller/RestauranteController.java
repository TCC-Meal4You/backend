package com.api.meal4you.controller;

import com.api.meal4you.dto.RestauranteFavoritoResponseDTO;
import com.api.meal4you.dto.RestaurantePorIdResponseDTO;
import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.dto.UsuarioAvaliaResponseDTO;
import com.api.meal4you.service.RestauranteService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ResponseEntity<List<RestauranteFavoritoResponseDTO>> listarRestaurantes() {
        List<RestauranteFavoritoResponseDTO> response = restauranteService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestauranteResponseDTO> atualizarRestaurantePorId(@PathVariable int id, @RequestBody RestauranteRequestDTO dto) {
        RestauranteResponseDTO response = restauranteService.atualizarPorAdmLogado(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRestaurantes(@PathVariable int id, @RequestParam String nomeConfirmacao) {
        restauranteService.deletarRestaurante(id, nomeConfirmacao);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/meu-restaurante")
    public ResponseEntity<RestauranteResponseDTO> buscarMeuRestaurante() {
        RestauranteResponseDTO response = restauranteService.buscarMeuRestaurante();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/listar-por-id/{id}")
    public ResponseEntity<RestaurantePorIdResponseDTO> listarPorId(@PathVariable int id, @RequestParam Integer numPagina) {
        RestaurantePorIdResponseDTO response = restauranteService.listarPorId(id, numPagina);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/avaliacoes")
    public ResponseEntity<List<UsuarioAvaliaResponseDTO>> listarAvaliacoesMeuRestaurante() {
        List<UsuarioAvaliaResponseDTO> response = restauranteService.listarAvaliacoesDoMeuRestaurante();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/avaliacoes")
    public ResponseEntity<List<UsuarioAvaliaResponseDTO>> listarAvaliacoesPorIdDoRestaurante(@PathVariable int id) {
        List<UsuarioAvaliaResponseDTO> response = restauranteService.listarAvaliacoesPorIdDoRestaurante(id);
        return ResponseEntity.ok(response);
    }
      
    @PostMapping("/{id}/favorito")
    public ResponseEntity<Void> alternarFavorito(@PathVariable int id) {
        restauranteService.alternarFavorito(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favoritos")
    public ResponseEntity<List<RestauranteFavoritoResponseDTO>> listarRestaurantesFavoritos() {
        List<RestauranteFavoritoResponseDTO> response = restauranteService.listarRestaurantesFavoritos();
        return ResponseEntity.ok(response);
    }
}
