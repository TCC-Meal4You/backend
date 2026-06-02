package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import org.springframework.web.server.ResponseStatusException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.api.meal4you.base.BaseUnitTest;
import com.api.meal4you.dto.PaginacaoRefeicoesResponseDTO;
import com.api.meal4you.dto.PesquisarRefeicaoComFiltroRequestDTO;
import com.api.meal4you.dto.RefeicaoAvaliaRequestDTO;
import com.api.meal4you.dto.RefeicaoRequestDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.RefeicaoAvalia;
import com.api.meal4you.entity.RefeicaoFavorito;
import com.api.meal4you.entity.RefeicaoIngrediente;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.Restricao;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.entity.IngredienteRestricao;
import com.api.meal4you.entity.UsuarioRestricao;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.RefeicaoAvaliaRepository;
import com.api.meal4you.repository.RefeicaoFavoritoRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.UsuarioRepository;

class RefeicaoServiceTest extends BaseUnitTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private RefeicaoRepository refeicaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

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
    private RefeicaoAvaliaRepository refeicaoAvaliaRepository;

    @Mock
    private RefeicaoFavoritoRepository refeicaoFavoritoRepository;

    @InjectMocks
    private RefeicaoService refeicaoService;

    @AfterEach
    void tearDown() {
        // limpar contexto
    }

        @Test
        void shouldCreateRefeicaoWhenValid() {
                RefeicaoRequestDTO dto = RefeicaoRequestDTO.builder()
                                .nome("RefOk")
                                .descricao("Desc")
                                .preco(new BigDecimal("12.50"))
                                .tipo("Prato")
                                .ingredientesIds(List.of(1))
                                .build();

                AdmRestaurante adm = new AdmRestaurante();
                adm.setIdAdmin(5);

                Restaurante restaurante = new Restaurante();
                restaurante.setAdmin(adm);

                Ingrediente ing = new Ingrediente();
                ing.setIdIngrediente(1);
                ing.setNome("Tomate");
                ing.setAdmin(adm);

                when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
                when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
                when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.of(restaurante));
                when(refeicaoRepository.existsByNomeAndRestaurante(dto.getNome(), restaurante)).thenReturn(false);
                when(ingredienteRepository.findAllById(dto.getIngredientesIds())).thenReturn(List.of(ing));
                when(refeicaoRepository.save(any(Refeicao.class))).thenAnswer(invocation -> invocation.getArgument(0));

                var response = refeicaoService.cadastrarRefeicao(dto);

                assertThat(response).isNotNull();
                verify(refeicaoRepository).save(any(Refeicao.class));
                verify(refeicaoIngredienteRepository).saveAll(any());
        }

        @Test
        void shouldListPaginacaoRefeicoes() {
                // garantir usuário autenticado para listar (lenient para evitar UnnecessaryStubbing)
                org.mockito.Mockito.lenient().when(usuarioService.getUsuarioLogadoEmail()).thenReturn("user@example.com");
                org.mockito.Mockito.lenient().when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new Usuario()));

                // criar 3 refeições disponíveis
                List<Refeicao> lista = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                        Refeicao r = new Refeicao();
                        r.setIdRefeicao(i + 1);
                        r.setNome("R" + i);
                        r.setDescricao("D");
                        r.setTipo("T");
                        r.setDisponivel(true);
                        r.setPreco(new BigDecimal("5.0"));
                                Restaurante rest = new Restaurante();
                                rest.setIdRestaurante(i + 10);
                                r.setRestaurante(rest);
                                r.setRefeicaoIngredientes(new ArrayList<>());
                        lista.add(r);
                }

                when(refeicaoRepository.findAllByDisponivelTrue()).thenReturn(lista);

                var resp = refeicaoService.listarTodas(null);

                assertThat(resp).isNotNull();
                assertThat(resp.getTotalPaginas()).isGreaterThanOrEqualTo(1);
        }

        @Test
        void shouldUpdateRefeicaoWhenChangesProvided() {
                Refeicao refeicao = new Refeicao();
                refeicao.setIdRefeicao(2);
                refeicao.setNome("OldName");
                refeicao.setDescricao("Old");
                refeicao.setTipo("Prato");
                refeicao.setPreco(new BigDecimal("10.00"));
                refeicao.setRefeicaoIngredientes(new ArrayList<>());

                AdmRestaurante adm = new AdmRestaurante();
                adm.setIdAdmin(11);
                Restaurante restaurante = new Restaurante();
                restaurante.setAdmin(adm);
                refeicao.setRestaurante(restaurante);

                RefeicaoRequestDTO dto = RefeicaoRequestDTO.builder()
                                .nome("NewName")
                                .descricao("NewDesc")
                                .preco(new BigDecimal("12.00"))
                                .tipo("Prato")
                                .build();

                when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
                when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
                when(refeicaoRepository.findById(2)).thenReturn(Optional.of(refeicao));
                when(refeicaoRepository.existsByNomeAndRestauranteAndIdRefeicaoNot(dto.getNome(), restaurante, 2)).thenReturn(false);
                when(refeicaoRepository.save(any(Refeicao.class))).thenAnswer(i -> i.getArgument(0));

                var resp = refeicaoService.atualizarRefeicao(2, dto);

                assertThat(resp).isNotNull();
                verify(refeicaoRepository).save(any(Refeicao.class));
        }

        @Test
        void shouldDeleteRefeicaoWhenAuthorized() {
                Refeicao refeicao = new Refeicao();
                refeicao.setIdRefeicao(3);
                AdmRestaurante adm = new AdmRestaurante();
                adm.setIdAdmin(20);
                Restaurante restaurante = new Restaurante();
                restaurante.setAdmin(adm);
                refeicao.setRestaurante(restaurante);

                when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
                when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
                when(refeicaoRepository.findById(3)).thenReturn(Optional.of(refeicao));

                refeicaoService.deletarRefeicao(3);

                verify(refeicaoIngredienteRepository).deleteByRefeicao(refeicao);
                verify(refeicaoRepository).delete(refeicao);
        }

    @Test
    void shouldThrowWhenNoIngredientsProvided() {
        RefeicaoRequestDTO dto = RefeicaoRequestDTO.builder()
                .nome("Ref1")
                .descricao("Desc")
                .preco(new BigDecimal("10.0"))
                .tipo("Prato")
                .ingredientesIds(List.of())
                .build();

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
        when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(new AdmRestaurante()));
        when(restauranteRepository.findByAdmin(any())).thenReturn(Optional.of(new Restaurante()));

        assertThatThrownBy(() -> refeicaoService.cadastrarRefeicao(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("A refeição deve ter pelo menos 1 ingrediente.");
    }

    @Test
    void shouldThrowWhenIngredientNotBelongToAdmin() {
        RefeicaoRequestDTO dto = RefeicaoRequestDTO.builder()
                .nome("Ref1")
                .descricao("Desc")
                .preco(new BigDecimal("10.0"))
                .tipo("Prato")
                .ingredientesIds(List.of(1))
                .build();

        AdmRestaurante adm = new AdmRestaurante();
        adm.setIdAdmin(10);

        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);

        Ingrediente ing = new Ingrediente();
        AdmRestaurante other = new AdmRestaurante();
        other.setIdAdmin(99);
        ing.setAdmin(other);

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
        when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
        when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.of(restaurante));
        when(refeicaoRepository.existsByNomeAndRestaurante(dto.getNome(), restaurante)).thenReturn(false);
        when(ingredienteRepository.findAllById(dto.getIngredientesIds())).thenReturn(List.of(ing));

        assertThatThrownBy(() -> refeicaoService.cadastrarRefeicao(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Você não pode adicionar ingredientes que não pertencem a você.");
    }

    @Test
    void shouldThrowWhenRefeicaoNameConflict() {
        RefeicaoRequestDTO dto = RefeicaoRequestDTO.builder()
                .nome("Ref1")
                .descricao("Desc")
                .preco(new BigDecimal("10.0"))
                .tipo("Prato")
                .ingredientesIds(List.of(1))
                .build();

        AdmRestaurante adm = new AdmRestaurante();
        adm.setIdAdmin(10);

        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
        when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
        when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.of(restaurante));
        when(refeicaoRepository.existsByNomeAndRestaurante(dto.getNome(), restaurante)).thenReturn(true);

        assertThatThrownBy(() -> refeicaoService.cadastrarRefeicao(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Você já possui uma refeição com este nome no seu cardápio.");
    }

    @Test
    void shouldThrowWhenUpdatingRefeicaoWithNoChanges() {
        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);
        refeicao.setNome("Ref1");
        refeicao.setDescricao("Desc");
        refeicao.setTipo("Prato");
        refeicao.setPreco(new BigDecimal("10.0"));

        AdmRestaurante adm = new AdmRestaurante();
        adm.setIdAdmin(10);
        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);
        refeicao.setRestaurante(restaurante);

        RefeicaoRequestDTO dto = RefeicaoRequestDTO.builder()
                .nome("Ref1")
                .descricao("Desc")
                .preco(new BigDecimal("10.0"))
                .tipo("Prato")
                .build();

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
        when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
        when(refeicaoRepository.findById(1)).thenReturn(Optional.of(refeicao));

        assertThatThrownBy(() -> refeicaoService.atualizarRefeicao(1, dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Nenhuma alteração detectada.");
    }

    @Test
    void shouldReturnEmptyPageWhenPageNumberTooHigh() {
        org.mockito.Mockito.lenient().when(usuarioService.getUsuarioLogadoEmail()).thenReturn("user@example.com");
        org.mockito.Mockito.lenient().when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new Usuario()));

        Refeicao r = new Refeicao();
        r.setDisponivel(true);
        r.setPreco(new BigDecimal("10.0"));
        r.setNome("R1");
        r.setDescricao("D");
        r.setTipo("T");

        when(refeicaoRepository.findAllByDisponivelTrue()).thenReturn(List.of(r));

        var resp = refeicaoService.listarTodas(5);

        assertThat(resp).isNotNull();
        assertThat(resp.getTotalPaginas()).isEqualTo(0);
    }

    @Test
    void shouldSearchRefeicoesWithFilteringAndCompatibility() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        Restricao restricao = new Restricao();
        restricao.setIdRestricao(1);
        UsuarioRestricao ur = UsuarioRestricao.builder().restricao(restricao).build();
        usuario.setUsuarioRestricoes(List.of(ur));

        Refeicao r1 = new Refeicao();
        r1.setIdRefeicao(1);
        r1.setNome("Salada");
        r1.setDescricao("Leve");
        r1.setTipo("Vegetariano");
        r1.setPreco(new BigDecimal("12.00"));
        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);
        r1.setRestaurante(restaurante);
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setAdmin(new AdmRestaurante());
        ingrediente.setRestricoes(List.of(IngredienteRestricao.builder().restricao(Restricao.builder().idRestricao(1).build()).build()));
        RefeicaoIngrediente ri = RefeicaoIngrediente.builder().refeicao(r1).ingrediente(ingrediente).build();
        r1.setRefeicaoIngredientes(List.of(ri));
        r1.setDisponivel(true);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findAllByDisponivelTrue()).thenReturn(List.of(r1));

        PesquisarRefeicaoComFiltroRequestDTO dto = new PesquisarRefeicaoComFiltroRequestDTO();
        dto.setNomeOuDescricao("Salada");
        dto.setTipo("Vegetariano");
        dto.setPrecoMaximo(new BigDecimal("20.00"));

        var result = refeicaoService.pesquisarComFiltro(dto, 1);

        assertThat(result).isNotNull();
        assertThat(result.getTotalPaginas()).isEqualTo(1);
    }

    @Test
    void shouldUpdateAvailabilityWhenStateChanges() {
        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(10);
        AdmRestaurante adm = new AdmRestaurante();
        adm.setIdAdmin(1);
        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);
        refeicao.setRestaurante(restaurante);
        refeicao.setRefeicaoIngredientes(List.of());
        refeicao.setDisponivel(true);
        refeicao.setPreco(new BigDecimal("10.0"));
        refeicao.setNome("R1");
        refeicao.setDescricao("D");
        refeicao.setTipo("T");

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
        when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
        when(refeicaoRepository.findById(10)).thenReturn(Optional.of(refeicao));

        var response = refeicaoService.atualizarDisponibilidade(10, false);

        assertThat(response).isNotNull();
        assertThat(response.getDisponivel()).isFalse();
        verify(refeicaoRepository).save(refeicao);
    }

    @Test
    void shouldToggleFavoriteRefeicaoWhenNotAlreadyFavorited() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(1)).thenReturn(Optional.of(refeicao));
        when(refeicaoFavoritoRepository.findByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(Optional.empty());

        refeicaoService.alternarFavoritoRefeicao(1);

        verify(refeicaoFavoritoRepository).save(any());
    }

    @Test
    void shouldRemoveFavoriteRefeicaoWhenAlreadyFavorited() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);

        RefeicaoFavorito favorito = new RefeicaoFavorito();
        favorito.setUsuario(usuario);
        favorito.setRefeicao(refeicao);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(1)).thenReturn(Optional.of(refeicao));
        when(refeicaoFavoritoRepository.findByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(Optional.of(favorito));

        refeicaoService.alternarFavoritoRefeicao(1);

        verify(refeicaoFavoritoRepository).delete(favorito);
    }

    @Test
    void shouldReturnEmptySearchResultWhenNoRefeicoesMatchFilter() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setUsuarioRestricoes(List.of());

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findAllByDisponivelTrue()).thenReturn(List.of());

        PesquisarRefeicaoComFiltroRequestDTO dto = new PesquisarRefeicaoComFiltroRequestDTO();
        dto.setNomeOuDescricao("Nada");

        var result = refeicaoService.pesquisarComFiltro(dto, 1);

        assertThat(result).isNotNull();
        assertThat(result.getTotalPaginas()).isEqualTo(0);
    }

    @Test
    void shouldListFavoriteRefeicoes() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);
        refeicao.setRestaurante(new Restaurante());

        RefeicaoFavorito favorito = new RefeicaoFavorito();
        favorito.setUsuario(usuario);
        favorito.setRefeicao(refeicao);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(refeicaoFavoritoRepository.findByUsuario(usuario)).thenReturn(List.of(favorito));

        var response = refeicaoService.listarRefeicoesFavoritas();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    void shouldListMyRefeicoesWhenAdminHasRestaurant() {
        String emailAdmLogado = "adm@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setIdAdmin(1);
        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);
        refeicao.setRestaurante(restaurante);
        refeicao.setRefeicaoIngredientes(List.of());

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.of(restaurante));
        when(refeicaoRepository.findByRestaurante(restaurante)).thenReturn(List.of(refeicao));

        var response = refeicaoService.listarMinhasRefeicoes();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    void shouldEvaluateRefeicaoSuccessfully() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);

        RefeicaoAvaliaRequestDTO dto = new RefeicaoAvaliaRequestDTO(1, 5, "Ótimo");

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(1)).thenReturn(Optional.of(refeicao));
        when(refeicaoAvaliaRepository.existsByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(false);
        when(refeicaoAvaliaRepository.save(any(RefeicaoAvalia.class))).thenAnswer(i -> i.getArgument(0));

        var response = refeicaoService.avaliarRefeicao(dto);

        assertThat(response).isNotNull();
        assertThat(response.getNota()).isEqualTo(5);
    }

    @Test
    void shouldUpdateRefeicaoEvaluationSuccessfully() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);

        RefeicaoAvalia avaliacao = new RefeicaoAvalia();
        avaliacao.setUsuario(usuario);
        avaliacao.setRefeicao(refeicao);
        avaliacao.setNota(3);
        avaliacao.setComentario("Bom");

        RefeicaoAvaliaRequestDTO dto = new RefeicaoAvaliaRequestDTO(1, 4, "Melhor");

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(1)).thenReturn(Optional.of(refeicao));
        when(refeicaoAvaliaRepository.findByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(Optional.of(avaliacao));
        when(refeicaoAvaliaRepository.save(any(RefeicaoAvalia.class))).thenAnswer(i -> i.getArgument(0));

        var response = refeicaoService.atualizarAvaliacaoRefeicao(dto);

        assertThat(response).isNotNull();
        assertThat(response.getNota()).isEqualTo(4);
        assertThat(response.getComentario()).isEqualTo("Melhor");
    }

    @Test
    void shouldDeleteRefeicaoEvaluationSuccessfully() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);

        RefeicaoAvalia avaliacao = new RefeicaoAvalia();
        avaliacao.setUsuario(usuario);
        avaliacao.setRefeicao(refeicao);
        avaliacao.setNota(5);
        avaliacao.setComentario("Ótimo");

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(1)).thenReturn(Optional.of(refeicao));
        when(refeicaoAvaliaRepository.findByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(Optional.of(avaliacao));

        var response = refeicaoService.deletarAvaliacaoRefeicao(1);

        assertThat(response).isNotNull();
        assertThat(response.getNota()).isEqualTo(5);
    }

    @Test
    void shouldListReviewsByRefeicaoId() {
        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);

        RefeicaoAvalia avaliacao = new RefeicaoAvalia();
        avaliacao.setRefeicao(refeicao);
        avaliacao.setUsuario(new Usuario());
        avaliacao.setNota(4);
        avaliacao.setComentario("Bom");

        when(refeicaoRepository.findById(1)).thenReturn(Optional.of(refeicao));
        when(refeicaoAvaliaRepository.findByRefeicao(refeicao)).thenReturn(List.of(avaliacao));

        var response = refeicaoService.listarAvaliacoesPorIdRefeicao(1);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }
    @Test
    void shouldCreateRefeicaoSuccessfully() {
        String emailAdmLogado = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setIdAdmin(1);
        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setIdIngrediente(1);
        ingrediente.setAdmin(adm);

        RefeicaoRequestDTO dto = new RefeicaoRequestDTO();
        dto.setNome("Nova Refeicao");
        dto.setDescricao("Delicioso");
        dto.setTipo("Comida");
        dto.setPreco(BigDecimal.valueOf(10));
        dto.setIngredientesIds(List.of(1));

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.of(restaurante));
        when(refeicaoRepository.existsByNomeAndRestaurante(dto.getNome(), restaurante)).thenReturn(false);
        when(ingredienteRepository.findAllById(dto.getIngredientesIds())).thenReturn(List.of(ingrediente));
        when(refeicaoRepository.save(any(Refeicao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = refeicaoService.cadastrarRefeicao(dto);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo(dto.getNome());
    }

    @Test
    void shouldThrowWhenCreateRefeicaoWithInvalidIngredientIds() {
        String emailAdmLogado = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setIdAdmin(1);
        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);

        RefeicaoRequestDTO dto = new RefeicaoRequestDTO();
        dto.setNome("Nova Refeicao");
        dto.setDescricao("Delicioso");
        dto.setTipo("Comida");
        dto.setPreco(BigDecimal.valueOf(10));
        dto.setIngredientesIds(List.of(1, 2));

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.of(restaurante));
        when(refeicaoRepository.existsByNomeAndRestaurante(dto.getNome(), restaurante)).thenReturn(false);
        when(ingredienteRepository.findAllById(dto.getIngredientesIds())).thenReturn(List.of(new Ingrediente()));

        assertThatThrownBy(() -> refeicaoService.cadastrarRefeicao(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Um ou mais IDs de ingredientes são inválidos.");
    }

    @Test
    void shouldReturnEmptyPageWhenListingAllRefeicoesWithInvalidPage() {
        String email = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        Refeicao refeicao = new Refeicao();
        refeicao.setDisponivel(true);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(email);
        when(refeicaoRepository.findAllByDisponivelTrue()).thenReturn(List.of(refeicao));

        var response = refeicaoService.listarTodas(2);

        assertThat(response).isNotNull();
        assertThat(response.getTotalPaginas()).isEqualTo(0);
        assertThat(response.getRefeicoes()).isEmpty();
    }

    @Test
    void shouldUpdateRefeicaoAvailability() {
        String emailAdmLogado = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setIdAdmin(1);
        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);
        refeicao.setDisponivel(false);
        refeicao.setRestaurante(restaurante);
        refeicao.setRefeicaoIngredientes(List.of());

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(refeicaoRepository.findById(1)).thenReturn(Optional.of(refeicao));

        var response = refeicaoService.atualizarDisponibilidade(1, true);

        assertThat(response).isNotNull();
        assertThat(response.getDisponivel()).isTrue();
    }

    @Test
    void shouldDeleteRefeicaoSuccessfully() {
        String emailAdmLogado = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setIdAdmin(1);
        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);

        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(1);
        refeicao.setRestaurante(restaurante);

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(refeicaoRepository.findById(1)).thenReturn(Optional.of(refeicao));

        refeicaoService.deletarRefeicao(1);

        verify(refeicaoIngredienteRepository).deleteByRefeicao(refeicao);
        verify(refeicaoRepository).delete(refeicao);
    }}
