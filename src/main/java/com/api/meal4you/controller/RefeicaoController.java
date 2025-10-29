package com.api.meal4you.controller;

import com.api.meal4you.service.RefeicaoService;
import com.api.meal4you.dto.AtualizarDisponibilidadeRequestDTO;
import com.api.meal4you.dto.RefeicaoRequestDTO;
import com.api.meal4you.dto.RefeicaoResponseDTO;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/refeicoes")
@RequiredArgsConstructor
public class RefeicaoController {

    private final RefeicaoService refeicaoService;

    @PostMapping
    public ResponseEntity<RefeicaoResponseDTO> cadastrarRefeicao(@RequestBody RefeicaoRequestDTO dto) {
        RefeicaoResponseDTO response = refeicaoService.cadastrarRefeicao(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RefeicaoResponseDTO>> listarMinhasRefeicoes() {
        List<RefeicaoResponseDTO> response = refeicaoService.listarMinhasRefeicoes();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RefeicaoResponseDTO> atualizarRefeicao(@PathVariable int id, @RequestBody RefeicaoRequestDTO dto) {
        RefeicaoResponseDTO response = refeicaoService.atualizarRefeicao(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRefeicao(@PathVariable int id) {
        refeicaoService.deletarRefeicao(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<RefeicaoResponseDTO> atualizarDisponibilidade(@PathVariable int id, @RequestBody AtualizarDisponibilidadeRequestDTO dto) {
        if (dto.getDisponivel() == null) {
            return ResponseEntity.badRequest().build();
        }
        RefeicaoResponseDTO response = refeicaoService.atualizarDisponibilidade(id, dto.getDisponivel());
        return ResponseEntity.ok(response);
    }
}
