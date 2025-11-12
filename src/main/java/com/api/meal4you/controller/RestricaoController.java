package com.api.meal4you.controller;

import com.api.meal4you.dto.RestricaoResponseDTO;
import com.api.meal4you.dto.SincronizacaoRequestDTO;
import com.api.meal4you.service.RestricaoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/restricoes")
@RequiredArgsConstructor
@Tag(name = "Restrições Alimentares", description = "Endpoints para listar restrições alimentares e sincronização com IA")
public class RestricaoController {

    private final RestricaoService restricaoService;

    @Operation(
        summary = "Listar todas as restrições alimentares",
        description = "Retorna a lista completa de restrições alimentares cadastradas no sistema. Estas restrições são usadas tanto por administradores (ao cadastrar ingredientes) quanto por usuários (ao definir suas preferências alimentares)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de restrições retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao listar restrições", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<RestricaoResponseDTO>> listarRestricoes() {
        List<RestricaoResponseDTO> response = restricaoService.listarTodas();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Sincronizar restrições com IA (Gemini)",
        description = "Utiliza a API do Google Gemini para gerar automaticamente novos grupos de restrições alimentares que ainda não existem no sistema. O processo é inteligente e evita duplicações, focando em grupos genéricos e de alto nível (ex: 'Glúten', 'Lactose', 'Oleaginosas').\n" + 
        "Requer senha de sincronização configurada no servidor. Endpoint público para permitir sincronização via scheduler ou manualmente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sincronização concluída. Retorna mensagem com quantidade de novas restrições adicionadas"),
        @ApiResponse(responseCode = "401", description = "Senha de sincronização inválida", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao sincronizar com IA", content = @Content)
    })
    @PostMapping("/sincronizar")
    public ResponseEntity<String> sincronizarComIA(@RequestBody SincronizacaoRequestDTO dto){
        String response = restricaoService.sincronizarComIA(dto);
        return ResponseEntity.ok(response);
    }
}