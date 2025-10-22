package com.api.meal4you.controller;

import com.api.meal4you.dto.RestricaoResponseDTO;
import com.api.meal4you.dto.SincronizacaoRequestDTO;
import com.api.meal4you.service.RestricaoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/restricoes")
@RequiredArgsConstructor
public class RestricaoController {

    private final RestricaoService restricaoService;

    @GetMapping
    public ResponseEntity<List<RestricaoResponseDTO>> listarRestricoes() {
        List<RestricaoResponseDTO> response = restricaoService.listarTodas();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<String> sincronizarComIA(@RequestBody SincronizacaoRequestDTO dto){
        String response = restricaoService.sincronizarComIA(dto);
        return ResponseEntity.ok(response);
    }
}