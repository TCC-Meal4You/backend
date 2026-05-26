package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.api.meal4you.dto.RedefinirSenhaRequestDTO;
import com.api.meal4you.dto.UsuarioRequestDTO;
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
    void shouldThrowWhenRequestingEmailChangeForSocialLoginUser() {
        String emailLogado = "social@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(emailLogado, null));

        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);
        usuario.setSenha(null);

        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.solicitarAlteracaoEmail("novo@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuário criado via social login. Não pode alterar e-mail.");

        verify(verificaEmailService, never()).gerarESalvarCodigo(any());
        verify(emailCodeSenderService, never()).enviarEmail(any(), any(), any());
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
}
