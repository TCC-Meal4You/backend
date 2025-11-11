package com.api.meal4you.controller;

import java.util.Map;

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

import com.api.meal4you.dto.AdmRestauranteRequestDTO;
import com.api.meal4you.dto.AdmRestauranteResponseDTO;
import com.api.meal4you.dto.AtualizarEmailRequestDTO;
import com.api.meal4you.dto.GoogleLoginRequestDTO;
import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.dto.VerificaEmailRequestDTO;
import com.api.meal4you.service.AdmRestauranteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
@Tag(name = "Administradores de Restaurante", description = "Endpoints para gerenciamento de administradores, autenticação e perfil")
public class AdmRestauranteController {

    private final AdmRestauranteService admRestauranteService;

    @Operation(
        summary = "Enviar código de verificação",
        description = "Envia um código de verificação para o e-mail fornecido. Este código é necessário para completar o cadastro de administrador. O código expira em 5 minutos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Código enviado com sucesso"),
        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao enviar código", content = @Content)
    })
    @PostMapping("/verifica-email")
    public ResponseEntity<Map<String, String>> enviarCodigo(@Valid @RequestBody VerificaEmailRequestDTO dto) {
        admRestauranteService.enviarCodigoVerificacao(dto.getEmail());
        return ResponseEntity.ok(Map.of("mensagem", "Código de verificação enviado para o e-mail."));
    }

    @Operation(
        summary = "Solicitar alteração de e-mail",
        description = "Envia um código de verificação para o novo e-mail. Administradores cadastrados via social login não podem alterar o e-mail."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Código enviado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Administrador criado via social login não pode alterar e-mail", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Novo e-mail é igual ao atual", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao solicitar alteração", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/solicitar-alteracao-email")
    public ResponseEntity<Map<String, String>> solicitarAlteracaoEmail(@Valid @RequestBody VerificaEmailRequestDTO dto) {
        admRestauranteService.solicitarAlteracaoEmail(dto.getEmail());
        return ResponseEntity.ok(Map.of("mensagem", "Código de verificação para alteração de e-mail enviado para o novo endereço."));
    }

    @Operation(
        summary = "Atualizar e-mail",
        description = "Confirma a alteração do e-mail do administrador com o código de verificação. Todos os tokens ativos serão invalidados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "E-mail atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Código de verificação inválido ou expirado", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao atualizar e-mail", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/atualizar-email")
    public ResponseEntity<AdmRestauranteResponseDTO> atualizarEmail(@Valid @RequestBody AtualizarEmailRequestDTO dto) {
        AdmRestauranteResponseDTO response = admRestauranteService.atualizarEmail(dto.getEmail(), dto.getCodigoVerificacao());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Cadastrar novo administrador",
        description = "Cria um novo administrador de restaurante no sistema. É necessário ter um código de verificação válido obtido através do endpoint /verifica-email."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Administrador cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Código de verificação inválido ou expirado", content = @Content),
        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado como administrador ou usuário", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao cadastrar administrador", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AdmRestauranteResponseDTO> cadastrarAdm(@RequestBody AdmRestauranteRequestDTO dto) {
        AdmRestauranteResponseDTO response = admRestauranteService.cadastrarAdm(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Buscar meu perfil",
        description = "Retorna as informações do perfil do administrador autenticado (que está logado)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar perfil", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<AdmRestauranteResponseDTO> buscarMeuPerfil() {
        AdmRestauranteResponseDTO response = admRestauranteService.buscarMeuPerfil();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Atualizar meu perfil",
        description = "Atualiza as informações do perfil do administrador autenticado (nome e/ou senha). Administradores criados via social login não podem definir senha."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nenhuma alteração detectada ou operação não permitida para login social", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao atualizar perfil", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping
    public ResponseEntity<AdmRestauranteResponseDTO> atualizarMeuPerfil(@RequestBody AdmRestauranteRequestDTO dto) {
        AdmRestauranteResponseDTO response = admRestauranteService.atualizarMeuPerfil(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Deletar minha conta",
        description = "Deleta permanentemente a conta do administrador autenticado. É necessário confirmar o e-mail para segurança. Remove também o restaurante associado, todas as refeições, ingredientes, avaliações e favoritos relacionados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conta deletada com sucesso"),
        @ApiResponse(responseCode = "400", description = "E-mail de confirmação incorreto", content = @Content),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao deletar conta", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Void> deletarMinhaConta(@RequestParam String email) {
        admRestauranteService.deletarMinhaConta(email);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Login de administrador",
        description = "Autentica um administrador de restaurante com e-mail e senha. Retorna um token JWT para ser usado nas requisições autenticadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao fazer login", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = admRestauranteService.fazerLogin(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Login com Google",
        description = "Autentica um administrador usando o Google OAuth2. Se o administrador não existir, cria uma nova conta automaticamente. Retorna um token JWT. Não pode ser usado se a conta Google já estiver vinculada a um usuário comum."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login com Google realizado com sucesso"),
        @ApiResponse(responseCode = "409", description = "Conta Google já vinculada a um usuário comum", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao fazer login com Google", content = @Content)
    })
    @PostMapping("/login/oauth2/google")
    public ResponseEntity<LoginResponseDTO> fazerloginComGoogle(@RequestBody GoogleLoginRequestDTO body) {
        String accessToken = body.getAccessToken();
        LoginResponseDTO response = admRestauranteService.fazerLoginComGoogle(accessToken);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Logout",
        description = "Invalida o token JWT atual do administrador. O administrador precisará fazer login novamente para obter um novo token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Token inválido", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao fazer logout", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String header) {
        admRestauranteService.logout(header);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Logout global",
        description = "Invalida todos os tokens JWT ativos do administrador autenticado. Útil para fazer logout de todos os dispositivos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout global realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Administrador não autenticado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro ao fazer logout global", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout-global")
    public ResponseEntity<Void> logoutGlobal() {
        admRestauranteService.logoutGlobal();
        return ResponseEntity.ok().build();
    }
}
