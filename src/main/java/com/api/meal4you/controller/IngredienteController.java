package com.api.meal4you.controller;

import com.api.meal4you.dto.IngredienteRequestDTO;
import com.api.meal4you.dto.IngredienteResponseDTO;
import com.api.meal4you.service.IngredienteService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ingredientes")
@RequiredArgsConstructor
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @PostMapping
    public ResponseEntity<IngredienteResponseDTO> cadastrarIngrediente(@RequestBody IngredienteRequestDTO dto) {
        IngredienteResponseDTO response = ingredienteService.cadastrarIngrediente(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<IngredienteResponseDTO>> listarMeusIngredientes() {
        List<IngredienteResponseDTO> response = ingredienteService.listarMeusIngredientes();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarIngrediente(@PathVariable int id) {
        ingredienteService.deletarIngrediente(id);
        return ResponseEntity.ok().build();
    }

}
