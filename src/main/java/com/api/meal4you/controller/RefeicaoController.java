package com.api.meal4you.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.meal4you.dto.AtualizarDisponibilidadeRequestDTO;
import com.api.meal4you.dto.PaginacaoRefeicoesResponseDTO;
import com.api.meal4you.dto.PesquisarRefeicaoComFiltroRequestDTO;
import com.api.meal4you.dto.RefeicaoRequestDTO;
import com.api.meal4you.dto.RefeicaoResponseDTO;
import com.api.meal4you.service.RefeicaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/refeicoes")
@RequiredArgsConstructor
@Tag(name = "Refeições", description = "Endpoints para gerenciamento de refeições do cardápio do restaurante. Métodos para administradores.")
public class RefeicaoController {

    private final RefeicaoService refeicaoService;

    @Operation(
        summary = "Cadastrar refeição",
        description = "Cria uma nova refeição no cardápio do restaurante do administrador autenticado. A refeição deve ter pelo menos 1 ingrediente e o nome deve ser único dentro do cardápio desse restaurante.\n" +
                      "Utilizado na tela de gerenciamento de refeições do restaurante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refeição cadastrada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Refeição deve ter pelo menos 1 ingrediente ou IDs de ingredientes inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Não pode adicionar ingredientes que não pertencem a você", content = @Content),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado. Cadastre um restaurante primeiro", content = @Content),
        @ApiResponse(responseCode = "409", description = "Já existe uma refeição com este nome no seu cardápio", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao cadastrar refeição", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<RefeicaoResponseDTO> cadastrarRefeicao(@RequestBody RefeicaoRequestDTO dto) {
        RefeicaoResponseDTO response = refeicaoService.cadastrarRefeicao(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Listar minhas refeições",
        description = "Retorna todas as refeições do cardápio do restaurante do administrador autenticado, incluindo disponíveis e indisponíveis.\n" +
                      "Utilizado na tela de gerenciamento de refeições do restaurante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de refeições retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado para este administrador", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao listar refeições", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<RefeicaoResponseDTO>> listarMinhasRefeicoes() {
        List<RefeicaoResponseDTO> response = refeicaoService.listarMinhasRefeicoes();
        return ResponseEntity.ok(response);
    }

        @Operation(
        summary = "Listar todas as refeições disponíveis",
        description = "Lista todas as refeições disponíveis de 10 em 10 para o usuário final. Utilizado na tela de busca de refeições do aplicativo."

    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de refeições retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao listar refeições", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/listar-todas")
    public ResponseEntity<PaginacaoRefeicoesResponseDTO> listarTodas(@RequestParam int pagina) {
        PaginacaoRefeicoesResponseDTO response = refeicaoService.listarTodas(pagina);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Pesquisar refeições com filtro",
        description = "Isso permite aos usuários pesquisar refeições com base em critérios específicos, como nome, descrição ou tipo de comida. Retorna uma lista paginada de refeições que correspondem aos filtros fornecidos. Cada página contém 10 refeições.\n" +
                      "Utilizado na tela de busca com filtros. Método para usuários."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de refeições filtrada retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao filtrar e listar refeições", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/pesquisar-com-filtro")
    public ResponseEntity<PaginacaoRefeicoesResponseDTO> pesquisarComFiltro(@RequestBody PesquisarRefeicaoComFiltroRequestDTO dto,@RequestParam Integer numPagina) {
        PaginacaoRefeicoesResponseDTO response = refeicaoService.pesquisarComFiltro(dto, numPagina);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Atualizar refeição",
        description = "Atualiza as informações de uma refeição do cardápio. Apenas o administrador dono do restaurante pode atualizar suas refeições. O novo nome não pode conflitar com outras refeições do mesmo cardápio.\n" +
                      "Utilizado na tela de gerenciamento de refeições do restaurante."

    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refeição atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nenhuma alteração detectada, refeição deve ter pelo menos 1 ingrediente ou IDs inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão para alterar esta refeição ou ingredientes não pertencem a você", content = @Content),
        @ApiResponse(responseCode = "404", description = "Refeição não encontrada", content = @Content),
        @ApiResponse(responseCode = "409", description = "Já existe outra refeição com este nome no seu cardápio", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao atualizar refeição", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<RefeicaoResponseDTO> atualizarRefeicao(@PathVariable int id, @RequestBody RefeicaoRequestDTO dto) {
        RefeicaoResponseDTO response = refeicaoService.atualizarRefeicao(id, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Deletar refeição",
        description = "Remove permanentemente uma refeição do cardápio. Apenas o administrador dono do restaurante pode deletar suas refeições. Remove também todas as associações com ingredientes.\n" +
                      "Utilizado na tela de gerenciamento de refeições do restaurante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refeição deletada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão para deletar esta refeição", content = @Content),
        @ApiResponse(responseCode = "404", description = "Refeição não encontrada", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao deletar refeição", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRefeicao(@PathVariable int id) {
        refeicaoService.deletarRefeicao(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Atualizar disponibilidade da refeição",
        description = "Altera o status de disponibilidade de uma refeição no cardápio (disponível/indisponível). Útil para marcar pratos que estão temporariamente fora do cardápio sem deletá-los.\n" +
                      "Apenas o administrador dono do restaurante pode atualizar.\n" +
                      "Utilizado na tela de gerenciamento de refeições do restaurante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Disponibilidade atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Campo disponível não informado ou refeição já está neste estado", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão para alterar esta refeição", content = @Content),
        @ApiResponse(responseCode = "404", description = "Refeição não encontrada", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao atualizar disponibilidade", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<RefeicaoResponseDTO> atualizarDisponibilidade(@PathVariable int id, @RequestBody AtualizarDisponibilidadeRequestDTO dto) {
        if (dto.getDisponivel() == null) {
            return ResponseEntity.badRequest().build();
        }
        RefeicaoResponseDTO response = refeicaoService.atualizarDisponibilidade(id, dto.getDisponivel());
        return ResponseEntity.ok(response);
    }
}
