package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
import com.api.meal4you.dto.AdmRestauranteRequestDTO;
import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.dto.RedefinirSenhaRequestDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.SocialLogin;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.IngredienteRestricaoRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteFavoritoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.SocialLoginRepository;
import com.api.meal4you.repository.UsuarioAvaliaRepository;
import com.api.meal4you.repository.UsuarioRepository;
import com.api.meal4you.security.JwtUtil;
import com.api.meal4you.security.TokenStore;

class AdmRestauranteServiceTest extends BaseUnitTest {

    @Mock
    private AdmRestauranteRepository admRepository;

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
    private SocialLoginRepository socialLoginRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private IngredienteRestricaoRepository ingredienteRestricaoRepository;

    @Mock
    private RefeicaoRepository refeicaoRepository;

    @Mock
    private RefeicaoIngredienteRepository refeicaoIngredienteRepository;

    @Mock
    private RestauranteFavoritoRepository restauranteFavoritoRepository;

    @Mock
    private UsuarioAvaliaRepository usuarioAvaliaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AdmRestauranteService admRestauranteService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowWhenAdminEmailAlreadyTakenDuringRegistration() {
        AdmRestauranteRequestDTO dto = new AdmRestauranteRequestDTO("Admin", "admin@example.com", "senha123", "123456");

        when(verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())).thenReturn(true);
        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(admRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new AdmRestaurante()));

        assertThatThrownBy(() -> admRestauranteService.cadastrarAdm(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail já cadastrado como administrador ou usuário.");
    }

    @Test
    void shouldRegisterAdminWhenEmailIsAvailableAndCodeIsValid() {
        AdmRestauranteRequestDTO dto = new AdmRestauranteRequestDTO("Admin", "admin@example.com", "senha123", "123456");

        when(verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())).thenReturn(true);
        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(admRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(encoder.encode(dto.getSenha())).thenReturn("encoded-senha");
        when(admRepository.saveAndFlush(any(AdmRestaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = admRestauranteService.cadastrarAdm(dto);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(dto.getEmail());
        verify(admRepository).saveAndFlush(any(AdmRestaurante.class));
    }

    @Test
    void shouldBlockSocialLoginAdminFromRequestingEmailChange() {
        String emailLogado = "social-admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailLogado);
        adm.setSenha(null);

        when(admRepository.findByEmail("novo@example.com")).thenReturn(Optional.empty());
        when(admRepository.findByEmail(emailLogado)).thenReturn(Optional.of(adm));

        assertThatThrownBy(() -> admRestauranteService.solicitarAlteracaoEmail("novo@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Administrador criado via social login. Não pode alterar e-mail.");
    }

    @Test
    void shouldThrowWhenDeletingAdminAccountWithIncorrectConfirmationEmail() {
        String emailLogado = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailLogado);
        adm.setSenha("encoded");

        when(admRepository.findByEmail(emailLogado)).thenReturn(Optional.of(adm));

        assertThatThrownBy(() -> admRestauranteService.deletarMinhaConta("wrong@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail de confirmação incorreto.");
    }

    @Test
    void shouldSendVerificationEmailWhenEmailIsAvailable() {
        String email = "admin@example.com";

        when(admRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(verificaEmailService.gerarESalvarCodigo(email)).thenReturn("123456");

        admRestauranteService.enviarCodigoVerificacao(email);

        verify(emailCodeSenderService).enviarEmail(eq(email), eq("Meal4You - Código de Verificação de Administrador"), org.mockito.ArgumentMatchers.contains("123456"));
    }

    @Test
    void shouldUpdateEmailWhenCodeIsValid() {
        String emailLogado = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailLogado);
        adm.setSenha("encoded-old");

        when(admRepository.findByEmail(emailLogado)).thenReturn(Optional.of(adm));
        when(verificaEmailService.validarCodigo("new@example.com", "654321")).thenReturn(true);

        var response = admRestauranteService.atualizarEmail("new@example.com", "654321");

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "ADMIN");
    }

    @Test
    void shouldRequestEmailChangeWhenUserHasPassword() {
        String emailLogado = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailLogado);
        adm.setSenha("encoded");

        when(admRepository.findByEmail(emailLogado)).thenReturn(Optional.of(adm));
        when(admRepository.findByEmail("novo@example.com")).thenReturn(Optional.empty());
        when(verificaEmailService.gerarESalvarCodigo("novo@example.com")).thenReturn("123456");

        admRestauranteService.solicitarAlteracaoEmail("novo@example.com");

        verify(emailCodeSenderService).enviarEmail(eq("novo@example.com"), eq("Meal4You - Confirmação de Alteração de E-mail"), org.mockito.ArgumentMatchers.contains("123456"));
    }

    @Test
    void shouldLoginAdminWithCorrectCredentials() {
        String email = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(email);
        adm.setSenha("encoded-pass");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setSenha("plain-pass");

        when(admRepository.findByEmail(email)).thenReturn(Optional.of(adm));
        when(encoder.matches("plain-pass", "encoded-pass")).thenReturn(true);
        when(jwtUtil.gerarToken(email, "ADMIN")).thenReturn("jwt-token-admin");

        var response = admRestauranteService.fazerLogin(dto);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-admin");
        verify(tokenStore).salvarToken("jwt-token-admin");
    }

    @Test
    void shouldThrowWhenGoogleAccountAlreadyLinkedToAdmin() {
        var googleUserInfo = new GooglePeopleApiService.GoogleUserInfo("google-id-1", "admin@example.com", "Admin Name");
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@example.com");
        SocialLogin socialLogin = SocialLogin.builder()
                .usuario(usuario)
                .provider("google")
                .providerId("google-id-1")
                .build();

        when(googlePeopleApiService.getUserInfo("access-token")).thenReturn(googleUserInfo);
        when(socialLoginRepository.findByProviderAndProviderId("google", "google-id-1")).thenReturn(Optional.of(socialLogin));

        assertThatThrownBy(() -> admRestauranteService.fazerLoginComGoogle("access-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Esta conta Google já está vinculada a uma conta de Usuário.");
    }

    @Test
    void shouldLogoutWhenHeaderIsValid() {
        admRestauranteService.logout("Bearer token-admin");

        verify(tokenStore).removerToken("token-admin");
    }

    @Test
    void shouldThrowWhenLogoutHeaderIsInvalid() {
        assertThatThrownBy(() -> admRestauranteService.logout("InvalidHeader"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token inválido ou ausente");
    }

    @Test
    void shouldReturnAdminProfileWhenAuthenticated() {
        String emailLogado = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailLogado);
        adm.setNome("Admin Test");

        when(admRepository.findByEmail(emailLogado)).thenReturn(Optional.of(adm));

        var response = admRestauranteService.buscarMeuPerfil();

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(emailLogado);
    }

    @Test
    void shouldUpdateAdminProfileNameAndPassword() {
        String emailLogado = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailLogado);
        adm.setSenha("encoded-old");
        adm.setNome("Old Name");

        when(admRepository.findByEmail(emailLogado)).thenReturn(Optional.of(adm));
        when(encoder.matches("newpass", "encoded-old")).thenReturn(false);
        when(encoder.encode("newpass")).thenReturn("encoded-new");

        AdmRestauranteRequestDTO dto = new AdmRestauranteRequestDTO("New Name", null, "newpass", null);

        var response = admRestauranteService.atualizarMeuPerfil(dto);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo("New Name");
        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "ADMIN");
    }

    @Test
    void shouldDeleteAdminAccountWhenNoRestaurantExists() {
        String emailLogado = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailLogado);
        adm.setSenha("encoded");

        when(admRepository.findByEmail(emailLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.empty());

        admRestauranteService.deletarMinhaConta(emailLogado);

        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "ADMIN");
        verify(admRepository).delete(adm);
    }

    @Test
    void shouldSendPasswordRecoveryEmailWhenAdminHasPassword() {
        String email = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(email);
        adm.setSenha("encoded");

        when(admRepository.findByEmail(email)).thenReturn(Optional.of(adm));
        when(verificaEmailService.gerarESalvarCodigo(email)).thenReturn("987654");

        admRestauranteService.enviarCodigoRedefinicaoSenha(email);

        verify(emailCodeSenderService).enviarEmail(eq(email), eq("Meal4You - Código de Redefinição de Senha"), org.mockito.ArgumentMatchers.contains("987654"));
    }

    @Test
    void shouldRedefinePasswordWhenCodeIsValid() {
        String email = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(email);
        adm.setSenha("encoded-old");

        when(verificaEmailService.validarCodigo(email, "123456")).thenReturn(true);
        when(admRepository.findByEmail(email)).thenReturn(Optional.of(adm));
        when(encoder.encode("novaSenha")).thenReturn("encoded-nova");

        var dto = new RedefinirSenhaRequestDTO(email, "123456", "novaSenha");
        admRestauranteService.redefinirSenha(dto);

        verify(admRepository).save(adm);
        assertThat(adm.getSenha()).isEqualTo("encoded-nova");
    }

    @Test
    void shouldLoginWithGoogleAndCreateNewAdminWhenNotFound() {
        var googleUserInfo = new GooglePeopleApiService.GoogleUserInfo("google-id-123", "admin@example.com", "Admin Google");

        when(googlePeopleApiService.getUserInfo("access-token")).thenReturn(googleUserInfo);
        when(socialLoginRepository.findByProviderAndProviderId("google", "google-id-123")).thenReturn(Optional.empty());
        when(admRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(admRepository.save(any(AdmRestaurante.class))).thenAnswer(invocation -> {
            AdmRestaurante savedAdmin = invocation.getArgument(0);
            savedAdmin.setSocialLogins(new java.util.ArrayList<>());
            return savedAdmin;
        });
        when(jwtUtil.gerarToken("admin@example.com", "ADMIN")).thenReturn("jwt-token");

        LoginResponseDTO response = admRestauranteService.fazerLoginComGoogle("access-token");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(socialLoginRepository).save(any(SocialLogin.class));
        verify(tokenStore).salvarToken("jwt-token");
    }

    @Test
    void shouldLoginWithGoogleWhenExistingGoogleAccountFound() {
        var googleUserInfo = new GooglePeopleApiService.GoogleUserInfo("google-id-123", "admin@example.com", "Admin Google");

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail("admin@example.com");
        SocialLogin socialLogin = SocialLogin.builder()
                .adm(adm)
                .provider("google")
                .providerId("google-id-123")
                .build();
        adm.setSocialLogins(List.of(socialLogin));

        when(googlePeopleApiService.getUserInfo("access-token")).thenReturn(googleUserInfo);
        when(socialLoginRepository.findByProviderAndProviderId("google", "google-id-123")).thenReturn(Optional.of(socialLogin));
        when(admRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adm));
        when(jwtUtil.gerarToken("admin@example.com", "ADMIN")).thenReturn("jwt-token-existing");

        LoginResponseDTO response = admRestauranteService.fazerLoginComGoogle("access-token");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-existing");
        verify(tokenStore).salvarToken("jwt-token-existing");
    }

    @Test
    void shouldLogoutGlobal() {
        String emailLogado = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        admRestauranteService.logoutGlobal();

        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "ADMIN");
    }

    @Test
    void shouldThrowWhenSendVerificationCodeEmailAlreadyRegistered() {
        String email = "admin@example.com";
        when(admRepository.findByEmail(email)).thenReturn(Optional.of(new AdmRestaurante()));

        assertThatThrownBy(() -> admRestauranteService.enviarCodigoVerificacao(email))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail já cadastrado");
    }

    @Test
    void shouldThrowWhenRequestEmailChangeEmailAlreadyInUse() {
        String emailLogado = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        when(admRepository.findByEmail("novo@example.com")).thenReturn(Optional.of(new AdmRestaurante()));

        assertThatThrownBy(() -> admRestauranteService.solicitarAlteracaoEmail("novo@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Este e-mail já está em uso.");
    }

    @Test
    void shouldThrowWhenLoginWithIncorrectPassword() {
        String email = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(email);
        adm.setSenha("encoded-pass");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setSenha("wrong-pass");

        when(admRepository.findByEmail(email)).thenReturn(Optional.of(adm));
        when(encoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

        assertThatThrownBy(() -> admRestauranteService.fazerLogin(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail ou senha incorreta");
    }

    @Test
    void shouldThrowWhenPasswordRecoveryRequestedForSocialLoginAdmin() {
        String email = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(email);
        adm.setSenha(null);

        when(admRepository.findByEmail(email)).thenReturn(Optional.of(adm));

        assertThatThrownBy(() -> admRestauranteService.enviarCodigoRedefinicaoSenha(email))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Administrador criado via social login. Não é possível redefinir senha.");
    }

    @Test
    void shouldThrowWhenRegisterAdminWithInvalidVerificationCode() {
        AdmRestauranteRequestDTO dto = new AdmRestauranteRequestDTO("Admin", "admin@example.com", "senha123", "invalid-code");

        when(verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())).thenReturn(false);

        assertThatThrownBy(() -> admRestauranteService.cadastrarAdm(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Código de verificação inválido ou expirado.");
    }

    @Test
    void shouldDeleteAdminAccountAndRelatedEntitiesWhenRestaurantExists() {
        String emailLogado = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setIdIngrediente(1);
        ingrediente.setAdmin(adm);

        when(admRepository.findByEmail(emailLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.of(restaurante));
        when(refeicaoRepository.findByRestaurante(restaurante)).thenReturn(List.of(refeicao));
        when(ingredienteRepository.findByAdmin(adm)).thenReturn(List.of(ingrediente));

        admRestauranteService.deletarMinhaConta(emailLogado);

        verify(refeicaoIngredienteRepository).deleteByRefeicao(refeicao);
        verify(refeicaoRepository).deleteAll(List.of(refeicao));
        verify(ingredienteRestricaoRepository).deleteByIngrediente(ingrediente);
        verify(ingredienteRepository).deleteAll(List.of(ingrediente));
        verify(usuarioAvaliaRepository).deleteByRestaurante(restaurante);
        verify(restauranteFavoritoRepository).deleteByRestaurante(restaurante);
        verify(restauranteRepository).delete(restaurante);
        verify(socialLoginRepository).deleteByAdm(adm);
        verify(tokenStore).removerTodosTokensDaPessoa(emailLogado, "ADMIN");
        verify(admRepository).delete(adm);
    }
}
