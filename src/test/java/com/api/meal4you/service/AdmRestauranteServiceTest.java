package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.api.meal4you.entity.AdmRestaurante;
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
}
