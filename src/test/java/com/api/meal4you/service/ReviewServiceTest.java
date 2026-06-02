package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.api.meal4you.base.BaseUnitTest;
import com.api.meal4you.dto.RefeicaoAvaliaRequestDTO;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.RefeicaoAvalia;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.RefeicaoAvaliaRepository;
import com.api.meal4you.repository.RefeicaoFavoritoRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.UsuarioRepository;

class ReviewServiceTest extends BaseUnitTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private RefeicaoRepository refeicaoRepository;

    @Mock
    private AdmRestauranteService admRestauranteService;

    @Mock
    private AdmRestauranteRepository admRestauranteRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private RefeicaoIngredienteRepository refeicaoIngredienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RefeicaoAvaliaRepository refeicaoAvaliaRepository;

    @Mock
    private RefeicaoFavoritoRepository refeicaoFavoritoRepository;

    @InjectMocks
    private RefeicaoService refeicaoService;

    @Test
    void shouldCreateReviewWhenNoteIsValidAndNoExistingReview() {
        String emailLogado = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(40);
        refeicao.setRestaurante(new Restaurante());

        RefeicaoAvaliaRequestDTO dto = RefeicaoAvaliaRequestDTO.builder()
                .idRefeicao(40)
                .nota(4)
                .comentario("Ótima refeição")
                .build();

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(emailLogado);
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(40)).thenReturn(Optional.of(refeicao));
        when(refeicaoAvaliaRepository.existsByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(false);
        when(refeicaoAvaliaRepository.save(any(RefeicaoAvalia.class))).thenAnswer(invocation -> {
            RefeicaoAvalia aval = invocation.getArgument(0);
            aval.setDataAvaliacao(LocalDate.now());
            return aval;
        });

        var response = refeicaoService.avaliarRefeicao(dto);

        assertThat(response).isNotNull();
        assertThat(response.getNota()).isEqualTo(4);
        assertThat(response.getComentario()).isEqualTo("Ótima refeição");
    }

    @Test
    void shouldThrowConflictWhenReviewAlreadyExists() {
        String emailLogado = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(41);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(emailLogado);
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(41)).thenReturn(Optional.of(refeicao));
        when(refeicaoAvaliaRepository.existsByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(true);

        RefeicaoAvaliaRequestDTO dto = RefeicaoAvaliaRequestDTO.builder()
                .idRefeicao(41)
                .nota(5)
                .comentario("Excelente")
                .build();

        assertThatThrownBy(() -> refeicaoService.avaliarRefeicao(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Você já avaliou esta refeição.");
    }

    @Test
    void shouldThrowBadRequestWhenScoreIsInvalid() {
        String emailLogado = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(42);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(emailLogado);
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(42)).thenReturn(Optional.of(refeicao));
        when(refeicaoAvaliaRepository.existsByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(false);

        RefeicaoAvaliaRequestDTO dto = RefeicaoAvaliaRequestDTO.builder()
                .idRefeicao(42)
                .nota(6)
                .comentario("Nota inválida")
                .build();

        assertThatThrownBy(() -> refeicaoService.avaliarRefeicao(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Nota inválida. Deve estar entre 0 e 5.");
    }

    @Test
    void shouldThrowUnauthorizedWhenUserNotAuthenticatedForReview() {
        when(usuarioService.getUsuarioLogadoEmail()).thenReturn("no-user@example.com");
        when(usuarioRepository.findByEmail("no-user@example.com")).thenReturn(Optional.empty());

        RefeicaoAvaliaRequestDTO dto = RefeicaoAvaliaRequestDTO.builder()
                .idRefeicao(43)
                .nota(3)
                .comentario("Boa")
                .build();

        assertThatThrownBy(() -> refeicaoService.avaliarRefeicao(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Usuário não autenticado.");
    }
}
