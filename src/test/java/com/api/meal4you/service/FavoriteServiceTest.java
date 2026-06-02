package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.api.meal4you.base.BaseUnitTest;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.RefeicaoFavorito;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.RestauranteFavorito;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.IngredienteRestricaoRepository;
import com.api.meal4you.repository.RefeicaoFavoritoRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteFavoritoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.UsuarioAvaliaRepository;
import com.api.meal4you.repository.UsuarioRepository;
import com.api.meal4you.service.AdmRestauranteService;
import com.api.meal4you.service.ViaCepService;

class FavoriteServiceTest extends BaseUnitTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private RestauranteFavoritoRepository restauranteFavoritoRepository;

    @Mock
    private RefeicaoRepository refeicaoRepository;

    @Mock
    private RefeicaoFavoritoRepository refeicaoFavoritoRepository;

    @Mock
    private AdmRestauranteService admRestauranteService;

    @Mock
    private AdmRestauranteRepository admRestauranteRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private IngredienteRestricaoRepository ingredienteRestricaoRepository;

    @Mock
    private RefeicaoIngredienteRepository refeicaoIngredienteRepository;

    @Mock
    private UsuarioAvaliaRepository usuarioAvaliaRepository;

    @Mock
    private ViaCepService viaCepService;

    @InjectMocks
    private RestauranteService restauranteService;

    @InjectMocks
    private RefeicaoService refeicaoService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setEmail("user@example.com");
    }

    @Test
    void shouldAddRestaurantToFavoritesWhenNotAlreadyFavorited() {
        int restauranteId = 15;
        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(restauranteId);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        when(restauranteFavoritoRepository.findByUsuarioAndRestaurante(usuario, restaurante)).thenReturn(Optional.empty());

        restauranteService.alternarFavorito(restauranteId);

        verify(restauranteFavoritoRepository).save(any(RestauranteFavorito.class));
    }

    @Test
    void shouldRemoveRestaurantFromFavoritesWhenAlreadyFavorited() {
        int restauranteId = 16;
        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(restauranteId);
        RestauranteFavorito favorito = new RestauranteFavorito();
        favorito.setRestaurante(restaurante);
        favorito.setUsuario(usuario);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        when(restauranteFavoritoRepository.findByUsuarioAndRestaurante(usuario, restaurante)).thenReturn(Optional.of(favorito));

        restauranteService.alternarFavorito(restauranteId);

        verify(restauranteFavoritoRepository).delete(favorito);
    }

    @Test
    void shouldThrowWhenUserNotAuthenticatedForRestaurantFavorite() {
        int restauranteId = 17;
        when(usuarioService.getUsuarioLogadoEmail()).thenReturn("user@invalid.com");
        when(usuarioRepository.findByEmail("user@invalid.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restauranteService.alternarFavorito(restauranteId))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Usuário não autenticado.");
    }

    @Test
    void shouldAddMealToFavoritesWhenNotAlreadyFavorited() {
        int refeicaoId = 21;
        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(refeicaoId);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(refeicaoId)).thenReturn(Optional.of(refeicao));
        when(refeicaoFavoritoRepository.findByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(Optional.empty());

        refeicaoService.alternarFavoritoRefeicao(refeicaoId);

        verify(refeicaoFavoritoRepository).save(any(RefeicaoFavorito.class));
    }

    @Test
    void shouldRemoveMealFromFavoritesWhenAlreadyFavorited() {
        int refeicaoId = 22;
        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(refeicaoId);
        RefeicaoFavorito favorito = new RefeicaoFavorito();
        favorito.setRefeicao(refeicao);
        favorito.setUsuario(usuario);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findById(refeicaoId)).thenReturn(Optional.of(refeicao));
        when(refeicaoFavoritoRepository.findByUsuarioAndRefeicao(usuario, refeicao)).thenReturn(Optional.of(favorito));

        refeicaoService.alternarFavoritoRefeicao(refeicaoId);

        verify(refeicaoFavoritoRepository).delete(favorito);
    }

    @Test
    void shouldThrowWhenUserNotAuthenticatedForMealFavorite() {
        int refeicaoId = 23;
        when(usuarioService.getUsuarioLogadoEmail()).thenReturn("user@invalid.com");
        when(usuarioRepository.findByEmail("user@invalid.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refeicaoService.alternarFavoritoRefeicao(refeicaoId))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Usuário não autenticado.");
    }

    @Test
    void shouldListFavoriteRestaurantsForAuthenticatedUser() {
        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(30);
        RestauranteFavorito favorito = new RestauranteFavorito();
        favorito.setRestaurante(restaurante);
        favorito.setUsuario(usuario);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(restauranteFavoritoRepository.findByUsuario(usuario)).thenReturn(List.of(favorito));

        var response = restauranteService.listarRestaurantesFavoritos();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    void shouldListFavoriteMealsForAuthenticatedUser() {
        Refeicao refeicao = new Refeicao();
        refeicao.setIdRefeicao(31);
        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(99);
        refeicao.setRestaurante(restaurante);

        RefeicaoFavorito favorito = new RefeicaoFavorito();
        favorito.setRefeicao(refeicao);
        favorito.setUsuario(usuario);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(refeicaoFavoritoRepository.findByUsuario(usuario)).thenReturn(List.of(favorito));

        var response = refeicaoService.listarRefeicoesFavoritas();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }
}
