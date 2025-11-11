package com.api.meal4you.controller;

import com.api.meal4you.dto.IngredienteRequestDTO;
import com.api.meal4you.dto.IngredienteResponseDTO;
import com.api.meal4you.service.IngredienteService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ingredientes")
@RequiredArgsConstructor
@Tag(name = "Ingredientes", description = "Endpoints para gerenciamento de ingredientes utilizados nas refeições. Métodos para administradores.")
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @Operation(
        summary = "Cadastrar ingrediente",
        description = "Cria um novo ingrediente associado ao administrador autenticado. O nome do ingrediente deve ser único para cada administrador. Pode vincular restrições alimentares opcionais ao ingrediente.\n" +
        "Requer que o administrador já tenha cadastrado um restaurante.\n" +
        "Utilizado na tela de gerenciamento de ingredientes do restaurante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ingrediente cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Um ou mais IDs de restrição são inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Você precisa cadastrar um restaurante para ter ingredientes", content = @Content),
        @ApiResponse(responseCode = "409", description = "Você já cadastrou um ingrediente com este nome", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao cadastrar ingrediente", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<IngredienteResponseDTO> cadastrarIngrediente(@RequestBody IngredienteRequestDTO dto) {
        IngredienteResponseDTO response = ingredienteService.cadastrarIngrediente(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Listar meus ingredientes",
        description = "Retorna todos os ingredientes cadastrados pelo administrador autenticado, incluindo suas restrições alimentares associadas.\n" + 
                      "Utilizado na tela de gerenciamento de ingredientes e na tela de criação de refeição do restaurante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de ingredientes retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Você precisa cadastrar um restaurante para ter ingredientes", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao listar ingredientes", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<IngredienteResponseDTO>> listarMeusIngredientes() {
        List<IngredienteResponseDTO> response = ingredienteService.listarMeusIngredientes();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Deletar ingrediente",
        description = "Remove permanentemente um ingrediente do sistema. Apenas o administrador dono do ingrediente pode deletá-lo. Não é possível deletar ingredientes que estão sendo usados em refeições.\n" + 
        "Remove também todas as associações com restrições alimentares.\n" +
        "Utilizado na tela de gerenciamento de ingredientes do restaurante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ingrediente deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Ingrediente não encontrado ou não pertence a você", content = @Content),
        @ApiResponse(responseCode = "409", description = "Ingrediente não pode ser deletado pois está em uso em uma ou mais refeições", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao deletar ingrediente", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarIngrediente(@PathVariable int id) {
        ingredienteService.deletarIngrediente(id);
        return ResponseEntity.ok().build();
    }

}
