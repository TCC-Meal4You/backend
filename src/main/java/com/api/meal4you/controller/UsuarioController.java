package com.api.meal4you.controller;

import java.util.List;
import java.util.Map;

import com.api.meal4you.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.meal4you.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários, autenticação, perfil e suas avaliações.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(
        summary = "Enviar código de verificação",
        description = "Envia um código de verificação para o e-mail fornecido. Este código é necessário para completar o cadastro de usuário. O código expira em 5 minutos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Código enviado com sucesso"),
        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao enviar código", content = @Content)
    })
    @PostMapping("/verifica-email")
    public ResponseEntity<Map<String, String>> enviarCodigo(@Valid @RequestBody VerificaEmailRequestDTO dto) {
        usuarioService.enviarCodigoVerificacao(dto.getEmail());
        return ResponseEntity.ok(Map.of("mensagem", "Código de verificação enviado para o e-mail."));
    }

    @Operation(
        summary = "Cadastrar novo usuário",
        description = "Cria um novo usuário no sistema. É necessário ter um código de verificação válido obtido através do endpoint /verifica-email."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Código de verificação inválido ou expirado", content = @Content),
        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao cadastrar usuário", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrarUsuario(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Buscar meu perfil",
        description = "Retorna as informações do perfil do usuário autenticado (que está logado)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar perfil", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<UsuarioResponseDTO> buscarMeuPerfil() {
        UsuarioResponseDTO response = usuarioService.buscarMeuPerfil();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Solicitar alteração de e-mail",
        description = "Envia um código de verificação para o novo e-mail. Usuários cadastrados via social login não podem alterar o e-mail."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Código enviado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Usuário criado via social login não pode alterar e-mail", content = @Content),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Novo e-mail é igual ao atual", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao solicitar alteração", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/solicitar-alteracao-email")
    public ResponseEntity<Map<String, String>> solicitarAlteracaoEmail(@Valid @RequestBody VerificaEmailRequestDTO dto) {
        usuarioService.solicitarAlteracaoEmail(dto.getEmail());
        return ResponseEntity.ok(Map.of("mensagem", "Código de verificação para alteração de e-mail enviado para o novo endereço."));
    }

    @Operation(
        summary = "Atualizar e-mail",
        description = "Confirma a alteração do e-mail do usuário com o código de verificação. Todos os tokens ativos serão invalidados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "E-mail atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Código de verificação inválido ou expirado", content = @Content),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao atualizar e-mail", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/atualizar-email")
    public ResponseEntity<UsuarioResponseDTO> atualizarEmail(@Valid @RequestBody AtualizarEmailRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.atualizarEmail(dto.getEmail(), dto.getCodigoVerificacao());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Atualizar meu perfil",
        description = "Atualiza as informações do perfil do usuário autenticado (nome e/ou senha). Usuários criados via social login não podem definir senha."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nenhuma alteração detectada ou senha inválida", content = @Content),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao atualizar perfil", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping
    public ResponseEntity<UsuarioResponseDTO> atualizarMeuPerfil(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.atualizarMeuPerfil(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Deletar minha conta",
        description = "Deleta permanentemente a conta do usuário autenticado. É necessário confirmar o e-mail para segurança. Remove todas as avaliações, favoritos e restrições associadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conta deletada com sucesso"),
        @ApiResponse(responseCode = "400", description = "E-mail de confirmação incorreto", content = @Content),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao deletar conta", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deletarMinhaConta(@RequestParam String email) {
        usuarioService.deletarMinhaConta(email);
        return ResponseEntity.ok(Map.of("mensagem", "Usuário deletado com sucesso."));
    }

    @Operation(
        summary = "Atualizar minhas restrições alimentares",
        description = "Define ou atualiza as restrições alimentares do usuário. As restrições anteriores são substituídas pelas novas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restrições atualizadas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Um ou mais IDs de restrição são inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao atualizar restrições", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/restricoes")
    public ResponseEntity<UsuarioResponseDTO> atualizarMinhasRestricoes(@RequestBody UsuarioRestricaoRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.atualizarMinhasRestricoes(dto);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Login de usuário",
        description = "Autentica um usuário com e-mail e senha. Retorna um token JWT para ser usado nas requisições autenticadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao fazer login", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = usuarioService.fazerLogin(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Login com Google",
        description = "Autentica um usuário usando o Google OAuth2. Se o usuário não existir, cria uma nova conta automaticamente. Retorna um token JWT. Não pode ser usado se a conta Google já estiver vinculada a um Administrador."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login com Google realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Conta vinculada a um administrador", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao fazer login com Google", content = @Content)
    })
    @PostMapping("/login/oauth2/google")
    public ResponseEntity<LoginResponseDTO> fazerloginComGoogle(@RequestBody GoogleLoginRequestDTO body) {
        String accessToken = body.getAccessToken();
        LoginResponseDTO response = usuarioService.fazerLoginComGoogle(accessToken);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Logout",
        description = "Invalida o token JWT atual do usuário. O usuário precisará fazer login novamente para obter um novo token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Token inválido", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao fazer logout", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String header) {
        usuarioService.logout(header);
        return ResponseEntity.ok(Map.of("mensagem", "Logout realizado com sucesso."));
    }

    @Operation(
        summary = "Logout global",
        description = "Invalida todos os tokens JWT ativos do usuário autenticado. Útil para fazer logout de todos os dispositivos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout global realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao fazer logout global", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout-global")
    public ResponseEntity<Map<String, String>> logoutGlobal() {
        usuarioService.logoutGlobal();
        return ResponseEntity.ok(Map.of("mensagem", "Logout global realizado com sucesso."));
    }

    @Operation(
        summary = "Avaliar restaurante",
        description = "Cria uma nova avaliação para um restaurante. O usuário só pode avaliar cada restaurante uma vez. Nota deve estar entre 0 e 5."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avaliação criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nota inválida (deve estar entre 0 e 5)", content = @Content),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Usuário já avaliou este restaurante", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao criar avaliação", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/avaliar")
    public ResponseEntity<UsuarioAvaliaResponseDTO> avaliarRestaurante(@RequestBody UsuarioAvaliaRequestDTO dto) {
        UsuarioAvaliaResponseDTO response = usuarioService.avaliarRestaurante(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Atualizar avaliação",
        description = "Atualiza uma avaliação existente do usuário. A data da avaliação será atualizada para a data atual."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avaliação atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nota inválida (deve estar entre 0 e 5)", content = @Content),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Restaurante ou avaliação não encontrados", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao atualizar avaliação", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/atualizar-avaliacao")
    public ResponseEntity<UsuarioAvaliaResponseDTO> atualizarAvaliacao(@RequestBody UsuarioAvaliaRequestDTO dto) {
        UsuarioAvaliaResponseDTO response = usuarioService.atualizarAvaliacao(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Ver minhas avaliações",
        description = "Retorna todas as avaliações de todos os restaurantes feitas pelo usuário autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de avaliações retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar avaliações", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/ver-minhas-avaliacoes")
    public ResponseEntity<List<UsuarioAvaliaResponseDTO>> verMinhasAvaliacoes() {
        List<UsuarioAvaliaResponseDTO> response = usuarioService.verMinhasAvaliacoes();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Excluir avaliação",
        description = "Remove uma avaliação específica de um restaurante feita pelo usuário autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avaliação excluída com sucesso"),
        @ApiResponse(responseCode = "400", description = "Restaurante ou avaliação não encontrados", content = @Content),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao excluir avaliação", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/excluir-avaliacao")
    public ResponseEntity<Map<String, String>> excluirAvaliacao(@RequestParam Integer idRestaurante) {
        usuarioService.deletarAvaliacao(idRestaurante);
        return ResponseEntity.ok(Map.of("mensagem", "Avaliação excluída com sucesso."));
    }
}