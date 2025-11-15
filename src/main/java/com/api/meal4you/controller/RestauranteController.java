package com.api.meal4you.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.meal4you.dto.PesquisaRestauranteResponseDTO;
import com.api.meal4you.dto.PesquisarRestauranteComFiltroRequestDTO;
import com.api.meal4you.dto.RestauranteFavoritoResponseDTO;
import com.api.meal4you.dto.RestaurantePorIdResponseDTO;
import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.dto.UsuarioAvaliaResponseDTO;
import com.api.meal4you.service.RestauranteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurantes")
@RequiredArgsConstructor
@Tag(name = "Restaurantes", description = "Endpoints para gerenciamento de restaurantes, favoritos e avaliações")
public class RestauranteController {

    private final RestauranteService restauranteService;

    @Operation(
        summary = "Cadastrar restaurante",
        description = "Cria um novo restaurante vinculado ao administrador autenticado. O administrador só pode ter um restaurante cadastrado. A combinação de nome e localização deve ser única.\n" +
                      "Utilizado na tela para cadastro de restaurante.\n " +
                      "Método para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurante cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Já existe um restaurante com esse nome e localização", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao cadastrar restaurante", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<RestauranteResponseDTO> cadastrarRestaurante(@RequestBody RestauranteRequestDTO dto) {
        RestauranteResponseDTO response = restauranteService.cadastrarRestaurante(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Listar todos os restaurantes",
        description = "Retorna uma lista paginada de todos os restaurantes cadastrados no sistema. Cada página contém 10 restaurantes. Indica quais restaurantes são favoritos do usuário autenticado.\n" +
                      "Utilizado na tela de busca.\n " +
                      "Método para usuários."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de restaurantes retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao listar restaurantes", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<PesquisaRestauranteResponseDTO> listarRestaurantes(@RequestParam Integer numPagina) {
        PesquisaRestauranteResponseDTO response = restauranteService.listarTodos(numPagina);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Atualizar restaurante",
        description = "Atualiza as informações de um restaurante. Apenas o administrador dono do restaurante pode atualizá-lo.\n" +
                       "Utilizado na tela de configurações do restaurante.\n " +
                       "Método para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurante atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nenhuma alteração detectada", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Administrador não pode acessar restaurante de outro administrador", content = @Content),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao atualizar restaurante", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<RestauranteResponseDTO> atualizarRestaurantePorId(@PathVariable int id, @RequestBody RestauranteRequestDTO dto) {
        RestauranteResponseDTO response = restauranteService.atualizarPorAdmLogado(id, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Deletar restaurante",
        description = "Remove permanentemente um restaurante e todos os seus dados relacionados (refeições, ingredientes, avaliações e favoritos).\n" +
                      "Apenas o administrador dono do restaurante pode deletá-lo. É necessário confirmar o nome do restaurante.\n" +
                      "Utilizado na tela de configurações do restaurante.\n " +
                      "Método para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurante deletado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nome de confirmação incorreto", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Administrador não pode deletar restaurante de outro administrador", content = @Content),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao deletar restaurante", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRestaurantes(@PathVariable int id, @RequestParam String nomeConfirmacao) {
        restauranteService.deletarRestaurante(id, nomeConfirmacao);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Buscar meu restaurante",
        description = "Retorna as informações do restaurante do administrador autenticado (Administrador logado).\n" +
                      "Utilizado na tela de configurações do restaurante.\n " +
                      "Método para administradores."

    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurante retornado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Administrador ou restaurante não encontrado. Cadastre primeiro", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar restaurante", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/meu-restaurante")
    public ResponseEntity<RestauranteResponseDTO> buscarMeuRestaurante() {
        RestauranteResponseDTO response = restauranteService.buscarMeuRestaurante();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Listar restaurante por ID",
        description = "Retorna informações detalhadas de um restaurante específico, incluindo uma lista paginada de suas refeições disponíveis. Cada página contém 10 refeições. Indica se o restaurante é favorito do usuário autenticado.\n" +
                        "Utilizado na tela de detalhes do restaurante.\n " +
                        "Método para usuários."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurante retornado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar restaurante", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/listar-por-id/{id}")
    public ResponseEntity<RestaurantePorIdResponseDTO> listarPorId(@PathVariable int id, @RequestParam Integer numPagina) {
        RestaurantePorIdResponseDTO response = restauranteService.listarPorId(id, numPagina);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Pesquisar restaurantes com filtro",
        description = "Isso permite aos usuários pesquisar restaurantes com base em critérios específicos, como nome, descrição ou tipo de comida. Retorna uma lista paginada de restaurantes que correspondem aos filtros fornecidos. Cada página contém 10 restaurantes.\n" +
                      "Utilizado na tela de busca com filtros. Método para usuários."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de restaurantes filtrada retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao filtrar e listar restaurantes", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/pesquisar-com-filtro")
    public ResponseEntity<PesquisaRestauranteResponseDTO> pesquisarComFiltro(@RequestBody PesquisarRestauranteComFiltroRequestDTO dto,@RequestParam Integer numPagina) {
        PesquisaRestauranteResponseDTO response = restauranteService.pesquisarComFiltro(dto, numPagina);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Listar avaliações do meu restaurante",
        description = "Retorna todas as avaliações feitas pelos usuários no restaurante do administrador autenticado.\n" +
                      "Utilizado na tela de avaliações e comentários do restaurante.\n " +
                      "Método para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de avaliações retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Administrador ou restaurante não encontrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao listar avaliações", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/avaliacoes")
    public ResponseEntity<List<UsuarioAvaliaResponseDTO>> listarAvaliacoesMeuRestaurante() {
        List<UsuarioAvaliaResponseDTO> response = restauranteService.listarAvaliacoesDoMeuRestaurante();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Listar avaliações de um restaurante",
        description = "Retorna todas as avaliações feitas por usuários em um restaurante específico.\n" +
                      "Utilizado na tela de detalhes na aba avaliações do restaurante.\n " +
                      "Método para usuários."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de avaliações retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao listar avaliações", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}/avaliacoes")
    public ResponseEntity<List<UsuarioAvaliaResponseDTO>> listarAvaliacoesPorIdDoRestaurante(@PathVariable int id) {
        List<UsuarioAvaliaResponseDTO> response = restauranteService.listarAvaliacoesPorIdDoRestaurante(id);
        return ResponseEntity.ok(response);
    }
      
    @Operation(
        summary = "Alternar favorito",
        description = "Adiciona ou remove um restaurante dos favoritos do usuário autenticado. Se o restaurante já for favorito, será removido. Se não for favorito, será adicionado.\n" +
                      "Utilizado na tela de home, detalhe(na aba cardapio também), busca (aba restaurante) e de favoritos.\n " +
                      "Método para usuários."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Favorito alternado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao alternar favorito", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/favorito")
    public ResponseEntity<Void> alternarFavorito(@PathVariable int id) {
        restauranteService.alternarFavorito(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Listar meus restaurantes favoritos",
        description = "Retorna a lista de todos os restaurantes marcados como favoritos pelo usuário autenticado.\n" +
                      "Utilizado na tela de favoritos.\n " +
                      "Método para usuários."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de favoritos retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao listar favoritos", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/favoritos")
    public ResponseEntity<List<RestauranteFavoritoResponseDTO>> listarRestaurantesFavoritos() {
        List<RestauranteFavoritoResponseDTO> response = restauranteService.listarRestaurantesFavoritos();
        return ResponseEntity.ok(response);
    }
}
