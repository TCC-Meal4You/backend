package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.api.meal4you.base.BaseUnitTest;
import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.RedefinirSenhaRequestDTO;
import com.api.meal4you.dto.UsuarioAvaliaRequestDTO;
import com.api.meal4you.dto.UsuarioRequestDTO;
import com.api.meal4you.dto.UsuarioRestricaoRequestDTO;
import com.api.meal4you.dto.RecomendacaoKNNResponseDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.UsuarioAvalia;
import com.api.meal4you.entity.Restricao;
import com.api.meal4you.entity.SocialLogin;
import com.api.meal4you.entity.UsuarioRestricao;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.RestauranteFavoritoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.RestricaoRepository;
import com.api.meal4you.repository.SocialLoginRepository;
import com.api.meal4you.repository.UsuarioAvaliaRepository;
import com.api.meal4you.repository.UsuarioRepository;
import com.api.meal4you.repository.UsuarioRestricaoRepository;
import com.api.meal4you.security.JwtUtil;
import com.api.meal4you.security.TokenStore;

class UsuarioServiceTest extends BaseUnitTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RestricaoRepository restricaoRepository;

    @Mock
    private SocialLoginRepository socialLoginRepository;

    @Mock
    private UsuarioRestricaoRepository usuarioRestricaoRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenStore tokenStore;

    @Mock
    private VerificaEmailService verificaEmailService;

    @Mock
    private EmailCodeSenderService emailCodeSenderService;

    @Mock
    private GooglePeopleApiService googlePeopleApiService;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private UsuarioAvaliaRepository usuarioAvaliaRepository;

    @Mock
    private RestauranteFavoritoRepository restauranteFavoritoRepository;

    @Mock
    private AdmRestauranteRepository admRepository;

    @Mock
    private RecomendacoesKNN RecomendacoesKNN;

    @InjectMocks
    private UsuarioService usuarioService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowWhenRegisteringUserWithEmailAlreadyRegistered() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Usuário teste", "user@example.com", "senha123", "123456");

        when(verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())).thenReturn(true);
        when(admRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> usuarioService.cadastrarUsuario(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail já cadastrado como administrador ou usuário.");

        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowWhenRegisteringUserWithInvalidVerificationCode() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Usuário teste", "user@example.com", "senha123", "123456");

        when(verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.cadastrarUsuario(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Código de verificação inválido ou expirado.");

        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldSendVerificationEmailWhenNewEmailIsAvailable() {
        String email = "newuser@example.com";

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(verificaEmailService.gerarESalvarCodigo(email)).thenReturn("654321");

        usuarioService.enviarCodigoVerificacao(email);

        verify(emailCodeSenderService).enviarEmail(eq(email), eq("Meal4You - Código de Verificação"), org.mockito.ArgumentMatchers.contains("654321"));
    }

    @Test
    void shouldRequestEmailChangeWhenUserHasPassword() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setSenha("encoded");

        when(usuarioRepository.findByEmail("novo@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(verificaEmailService.gerarESalvarCodigo("novo@example.com")).thenReturn("123456");

        usuarioService.solicitarAlteracaoEmail("novo@example.com");

        verify(emailCodeSenderService).enviarEmail(eq("novo@example.com"), eq("Meal4You - Confirmação de Alteração de E-mail"), org.mockito.ArgumentMatchers.contains("123456"));
    }

    @Test
    void shouldThrowWhenUpdatingEmailWithInvalidCode() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setSenha("encoded");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(verificaEmailService.validarCodigo("new@example.com", "invalid")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.atualizarEmail("new@example.com", "invalid"))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Código de verificação inválido ou expirado.");
    }

    @Test
    void shouldThrowWhenUpdatingProfileWithoutChanges() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setNome("Same Name");
        usuario.setSenha("encoded-old");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(encoder.matches("newpass", "encoded-old")).thenReturn(true);

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Same Name");
        dto.setSenha("newpass");

        assertThatThrownBy(() -> usuarioService.atualizarMeuPerfil(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Nenhuma alteração detectada.");
    }

    @Test
    void shouldThrowWhenSocialLoginUserTriesToDefinePassword() {
        String emailLogado = "social@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setSenha(null);

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setSenha("newpass");

        assertThatThrownBy(() -> usuarioService.atualizarMeuPerfil(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Usuário criado via social login. Não pode definir senha.");
    }

    @Test
    void shouldUpdateRestrictionsToEmptyList() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setUsuarioRestricoes(new java.util.ArrayList<>());

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));

        UsuarioRestricaoRequestDTO dto = new UsuarioRestricaoRequestDTO(java.util.List.of());

        var response = usuarioService.atualizarMinhasRestricoes(dto);

        assertThat(response).isNotNull();
        verify(usuarioRestricaoRepository).deleteByUsuario(usuario);
    }

    @Test
    void shouldThrowWhenDeleteMyAccountWithWrongEmail() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setSenha("encoded");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.deletarMinhaConta("wrong@example.com"))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("E-mail incorreto");
    }

    @Test
    void shouldInvalidateTokensWhenPasswordChangeIsRequested() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setSenha("encoded-old");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(encoder.matches("newpass", "encoded-old")).thenReturn(false);
        when(encoder.encode("newpass")).thenReturn("encoded-new");

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setSenha("newpass");

        usuarioService.atualizarMeuPerfil(dto);

        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "USUARIO");
        verify(usuarioRepository).save(usuario);
        assertThat(usuario.getSenha()).isEqualTo("encoded-new");
    }

    @Test
    void shouldThrowWhenSendPasswordRecoveryForSocialLoginUser() {
        String email = "social@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha(null);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.enviarCodigoRedefinicaoSenha(email))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuário criado via social login. Não é possível redefinir senha.");

        verify(emailCodeSenderService, never()).enviarEmail(any(), any(), any());
    }

    @Test
    void shouldRedefinePasswordWhenVerificationCodeIsValid() {
        String email = "user@example.com";
        String codigo = "222222";

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha("encoded-old");

        when(verificaEmailService.validarCodigo(email, codigo)).thenReturn(true);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(encoder.encode("novaSenha")).thenReturn("encoded-nova");

        RedefinirSenhaRequestDTO dto = new RedefinirSenhaRequestDTO(email, codigo, "novaSenha");
        usuarioService.redefinirSenha(dto);

        verify(usuarioRepository).save(usuario);
        assertThat(usuario.getSenha()).isEqualTo("encoded-nova");
    }

    @Test
    void shouldThrowWhenLogoutHeaderIsInvalid() {
        assertThatThrownBy(() -> usuarioService.logout("InvalidHeader"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token inválido ou ausente");
    }

    @Test
    void shouldReturnMealRecommendationsForAuthenticatedUser() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(99);
        usuario.setEmail(emailLogado);

        RecomendacaoKNNResponseDTO expected = new RecomendacaoKNNResponseDTO(99, java.util.List.of(301, 302), "OK");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(RecomendacoesKNN.obterRecomendacaoRefeicaoKnn(99)).thenReturn(expected);

        RecomendacaoKNNResponseDTO result = usuarioService.obterRecomendacoesKnnRefeicoes();

        assertThat(result).isNotNull();
        assertThat(result.getIdUsuario()).isEqualTo(99);
        assertThat(result.getRecomendacoes()).containsExactly(301, 302);
    }

    @Test
    void shouldReturnRestaurantRecommendationsForAuthenticatedUser() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(100);
        usuario.setEmail(emailLogado);

        RecomendacaoKNNResponseDTO expected = new RecomendacaoKNNResponseDTO(100, java.util.List.of(401, 402), "OK");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(RecomendacoesKNN.obterRecomendacaoRestauranteKnn(100)).thenReturn(expected);

        RecomendacaoKNNResponseDTO result = usuarioService.obterRecomendacoesKnnRestaurantes();

        assertThat(result).isNotNull();
        assertThat(result.getIdUsuario()).isEqualTo(100);
        assertThat(result.getRecomendacoes()).containsExactly(401, 402);
    }

    @Test
    void shouldThrowUnauthorizedWhenRequestingMealRecommendationsAndUserNotFound() {
        String emailLogado = "missing@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obterRecomendacoesKnnRefeicoes())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuário não autenticado.");
    }

    @Test
    void shouldReturnAuthenticatedUserProfile() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setNome("User Name");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));

        var response = usuarioService.buscarMeuPerfil();

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(emailLogado);
    }

    @Test
    void shouldDeleteMyAccountWhenEmailMatches() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setSenha("encoded");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));

        usuarioService.deletarMinhaConta(emailLogado);

        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "USUARIO");
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void shouldUpdateEmailWhenVerificationCodeIsValid() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setSenha("encoded");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(verificaEmailService.validarCodigo("new@example.com", "111222")).thenReturn(true);

        var response = usuarioService.atualizarEmail("new@example.com", "111222");

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "USUARIO");
    }

    @Test
    void shouldUpdateMyProfileNameAndPassword() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setSenha("encoded-old");
        usuario.setNome("Old Name");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(encoder.matches("newpass", "encoded-old")).thenReturn(false);
        when(encoder.encode("newpass")).thenReturn("encoded-new");

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("New Name");
        dto.setSenha("newpass");

        var response = usuarioService.atualizarMeuPerfil(dto);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo("New Name");
        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "USUARIO");
    }

    @Test
    void shouldUpdateMyRestrictionsWhenListIsValid() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setUsuarioRestricoes(new java.util.ArrayList<>());

        Restricao restricao1 = new Restricao();
        restricao1.setIdRestricao(1);
        Restricao restricao2 = new Restricao();
        restricao2.setIdRestricao(2);

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restricaoRepository.findAllById(java.util.List.of(1, 2))).thenReturn(java.util.List.of(restricao1, restricao2));
        when(usuarioRestricaoRepository.saveAll(any())).thenReturn(java.util.List.of());

        UsuarioRestricaoRequestDTO dto = new UsuarioRestricaoRequestDTO(java.util.List.of(1, 2));

        var response = usuarioService.atualizarMinhasRestricoes(dto);

        assertThat(response).isNotNull();
        verify(usuarioRestricaoRepository).deleteByUsuario(usuario);
        verify(usuarioRestricaoRepository).saveAll(any());
    }

    @Test
    void shouldSendPasswordRecoveryEmailWhenUserHasPassword() {
        String email = "user@example.com";

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha("encoded");

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(verificaEmailService.gerarESalvarCodigo(email)).thenReturn("999999");

        usuarioService.enviarCodigoRedefinicaoSenha(email);

        verify(emailCodeSenderService).enviarEmail(eq(email), eq("Meal4You - Código de Redefinição de Senha"), org.mockito.ArgumentMatchers.contains("999999"));
    }

    @Test
    void shouldRegisterUserWhenEmailAndCodeValid() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Teste", "user@example.com", "senha123", "123456");
        when(verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())).thenReturn(true);
        when(admRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(encoder.encode(dto.getSenha())).thenReturn("encoded-senha");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = usuarioService.cadastrarUsuario(dto);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(dto.getEmail());
        verify(usuarioRepository).saveAndFlush(any(Usuario.class));
    }

    @Test
    void shouldThrowWhenLoginWithIncorrectPassword() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha("encoded-pass");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setSenha("wrong-pass");

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(encoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.fazerLogin(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Email ou senha incorreta");
    }

    @Test
    void shouldLogoutWithValidBearerHeader() {
        usuarioService.logout("Bearer valid-token");

        verify(tokenStore).removerToken("valid-token");
    }

    @Test
    void shouldCreateGoogleUserWhenNotFound() {
        var googleUserInfo = new GooglePeopleApiService.GoogleUserInfo("google-id-1", "user@example.com", "User Name");
        when(googlePeopleApiService.getUserInfo("access-token")).thenReturn(googleUserInfo);
        when(socialLoginRepository.findByProviderAndProviderId("google", "google-id-1")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario saved = invocation.getArgument(0);
            saved.setSocialLogins(new java.util.ArrayList<>());
            return saved;
        });
        when(jwtUtil.gerarToken("user@example.com", "USUARIO")).thenReturn("jwt-token-google");

        var response = usuarioService.fazerLoginComGoogle("access-token");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-google");
        verify(socialLoginRepository).save(any(SocialLogin.class));
        verify(tokenStore).salvarToken("jwt-token-google");
    }

    @Test
    void shouldThrowWhenGoogleAccountLinkedToAdmin() {
        var googleUserInfo = new GooglePeopleApiService.GoogleUserInfo("google-id-1", "user@example.com", "User Name");
        AdmRestaurante adm = new AdmRestaurante();
        SocialLogin socialLogin = SocialLogin.builder()
                .adm(adm)
                .provider("google")
                .providerId("google-id-1")
                .build();

        when(googlePeopleApiService.getUserInfo("access-token")).thenReturn(googleUserInfo);
        when(socialLoginRepository.findByProviderAndProviderId("google", "google-id-1")).thenReturn(Optional.of(socialLogin));

        assertThatThrownBy(() -> usuarioService.fazerLoginComGoogle("access-token"))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Esta conta Google já está vinculada a uma conta de Administrador.");
    }

    @Test
    void shouldLoginWithCorrectCredentials() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha("encoded-pass");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setSenha("plain-pass");

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(encoder.matches("plain-pass", "encoded-pass")).thenReturn(true);
        when(jwtUtil.gerarToken(email, "USUARIO")).thenReturn("jwt-token");

        var response = usuarioService.fazerLogin(dto);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(tokenStore).salvarToken("jwt-token");
    }

    @Test
    void shouldLogoutGlobalForAuthenticatedUser() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        usuarioService.logoutGlobal();

        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "USUARIO");
    }

    @Test
    void shouldSendVerificationEmailWhenEmailIsAvailable() {
        String email = "user@example.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(verificaEmailService.gerarESalvarCodigo(email)).thenReturn("543210");

        usuarioService.enviarCodigoVerificacao(email);

        verify(emailCodeSenderService).enviarEmail(eq(email), eq("Meal4You - Código de Verificação"), org.mockito.ArgumentMatchers.contains("543210"));
    }

    @Test
    void shouldThrowWhenRequestEmailChangeEmailAlreadyInUse() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        when(usuarioRepository.findByEmail("novo@example.com")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> usuarioService.solicitarAlteracaoEmail("novo@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Esse e-mail já está em uso.");
    }

    @Test
    void shouldDeleteMyAccountWhenConfirmed() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));

        usuarioService.deletarMinhaConta(emailLogado);

        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "USUARIO");
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void shouldUpdateEmailWhenCodeIsValid() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(verificaEmailService.validarCodigo("new@example.com", "654321")).thenReturn(true);

        var response = usuarioService.atualizarEmail("new@example.com", "654321");

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "USUARIO");
    }

    @Test
    void shouldThrowWhenUpdateProfileWithoutChanges() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setNome("Same Name");
        usuario.setSenha("encoded-pass");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(encoder.matches("samepass", "encoded-pass")).thenReturn(true);

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Same Name");
        dto.setSenha("samepass");

        assertThatThrownBy(() -> usuarioService.atualizarMeuPerfil(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nenhuma alteração detectada.");
    }

    @Test
    void shouldEvaluateRestaurantSuccessfully() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);

        UsuarioAvaliaRequestDTO dto = new UsuarioAvaliaRequestDTO(1, 5, "Excelente");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(usuarioAvaliaRepository.existsByUsuarioAndRestaurante(usuario, restaurante)).thenReturn(false);
        when(usuarioAvaliaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = usuarioService.avaliarRestaurante(dto);

        assertThat(response).isNotNull();
        assertThat(response.getNota()).isEqualTo(5);
    }

    @Test
    void shouldUpdateRestaurantEvaluationSuccessfully() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);

        UsuarioAvalia avaliacao = new UsuarioAvalia();
        avaliacao.setUsuario(usuario);
        avaliacao.setRestaurante(restaurante);
        avaliacao.setNota(3);
        avaliacao.setComentario("Bom");

        UsuarioAvaliaRequestDTO dto = new UsuarioAvaliaRequestDTO(1, 4, "Melhor");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(usuarioAvaliaRepository.findByUsuarioAndRestaurante(usuario, restaurante)).thenReturn(Optional.of(avaliacao));
        when(usuarioAvaliaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = usuarioService.atualizarAvaliacao(dto);

        assertThat(response).isNotNull();
        assertThat(response.getNota()).isEqualTo(4);
        assertThat(response.getComentario()).isEqualTo("Melhor");
    }

    @Test
    void shouldDeleteRestaurantEvaluationSuccessfully() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);

        UsuarioAvalia avaliacao = new UsuarioAvalia();
        avaliacao.setUsuario(usuario);
        avaliacao.setRestaurante(restaurante);
        avaliacao.setNota(5);
        avaliacao.setComentario("Ótimo");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(usuarioAvaliaRepository.findByUsuarioAndRestaurante(usuario, restaurante)).thenReturn(Optional.of(avaliacao));

        var response = usuarioService.deletarAvaliacao(1);

        assertThat(response).isNotNull();
        assertThat(response.getNota()).isEqualTo(5);
    }

    @Test
    void shouldListMyEvaluations() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        UsuarioAvalia avaliacao = new UsuarioAvalia();
        avaliacao.setUsuario(usuario);
        avaliacao.setRestaurante(new Restaurante());
        avaliacao.setNota(4);
        avaliacao.setComentario("Bom");

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(usuarioAvaliaRepository.findByUsuario(usuario)).thenReturn(List.of(avaliacao));

        var response = usuarioService.verMinhasAvaliacoes();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    void shouldRegisterUsuarioWhenValid() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("User");
        dto.setEmail("user@example.com");
        dto.setSenha("plain-pass");
        dto.setCodigoVerificacao("123456");

        when(verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())).thenReturn(true);
        when(admRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(encoder.encode(dto.getSenha())).thenReturn("encoded-pass");
        when(usuarioRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = usuarioService.cadastrarUsuario(dto);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void shouldThrowWhenRegisterUsuarioEmailAlreadyTakenByAdmin() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("User");
        dto.setEmail("admin@example.com");
        dto.setSenha("plain-pass");
        dto.setCodigoVerificacao("123456");

        when(verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())).thenReturn(true);
        when(admRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new AdmRestaurante()));

        assertThatThrownBy(() -> usuarioService.cadastrarUsuario(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail já cadastrado como administrador ou usuário.");
    }

    @Test
    void shouldThrowWhenUpdatingRestrictionsWithInvalidId() {
        String emailLogado = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setUsuarioRestricoes(new ArrayList<>());

        UsuarioRestricaoRequestDTO dto = new UsuarioRestricaoRequestDTO();
        dto.setRestricaoIds(List.of(1, 2));

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restricaoRepository.findAllById(dto.getRestricaoIds())).thenReturn(List.of(new Restricao()));

        assertThatThrownBy(() -> usuarioService.atualizarMinhasRestricoes(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Um ou mais IDs de restrição são inválidos.");
    }
}
