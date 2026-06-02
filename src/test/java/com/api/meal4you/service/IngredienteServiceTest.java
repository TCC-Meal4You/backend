package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.api.meal4you.dto.IngredienteRequestDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.IngredienteRestricao;
import com.api.meal4you.entity.Restricao;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.IngredienteRestricaoRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.RestricaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IngredienteServiceTest {

    @Mock
    private AdmRestauranteService admRestauranteService;

    @Mock
    private AdmRestauranteRepository admRestauranteRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private IngredienteRestricaoRepository ingredienteRestricaoRepository;

    @Mock
    private RestricaoRepository restricaoRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private RefeicaoIngredienteRepository refeicaoIngredienteRepository;

    @InjectMocks
    private IngredienteService ingredienteService;

    @Test
    void shouldThrowUnauthorizedWhenAdminNotAuthenticated() {
        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.cadastrarIngrediente(
                IngredienteRequestDTO.builder().nome("Tomate").build()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Administrador não autenticado");
    }

    @Test
    void shouldThrowConflictWhenIngredientAlreadyExists() {
        AdmRestaurante admin = AdmRestaurante.builder().idAdmin(1).email("admin@example.com").build();
        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(restauranteRepository.findByAdmin(admin)).thenReturn(Optional.of(Restaurante.builder().build()));
        when(ingredienteRepository.existsByNomeAndAdmin("Tomate", admin)).thenReturn(true);

        assertThatThrownBy(() -> ingredienteService.cadastrarIngrediente(
                IngredienteRequestDTO.builder().nome("Tomate").build()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Você já cadastrou um ingrediente com este nome");
    }

    @Test
    void shouldCreateIngredientWhenValidRequestWithoutRestrictions() {
        AdmRestaurante admin = AdmRestaurante.builder().idAdmin(1).email("admin@example.com").build();
        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(restauranteRepository.findByAdmin(admin)).thenReturn(Optional.of(Restaurante.builder().build()));
        when(ingredienteRepository.existsByNomeAndAdmin("Tomate", admin)).thenReturn(false);
        when(ingredienteRepository.save(any(Ingrediente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = ingredienteService.cadastrarIngrediente(
                IngredienteRequestDTO.builder().nome("Tomate").build());

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo("Tomate");
        assertThat(response.getRestricoes()).isEmpty();
    }

    @Test
    void shouldDeleteIngredientWhenNotInUse() {
        AdmRestaurante admin = AdmRestaurante.builder().idAdmin(1).email("admin@example.com").build();
        Ingrediente ingrediente = Ingrediente.builder().idIngrediente(2).nome("Tomate").admin(admin).build();

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(ingredienteRepository.findByIdIngredienteAndAdmin(2, admin)).thenReturn(Optional.of(ingrediente));
        when(refeicaoIngredienteRepository.existsByIngrediente(ingrediente)).thenReturn(false);
        doNothing().when(ingredienteRestricaoRepository).deleteByIngrediente(ingrediente);

        ingredienteService.deletarIngrediente(2);

        verify(ingredienteRestricaoRepository).deleteByIngrediente(ingrediente);
        verify(ingredienteRepository).delete(ingrediente);
    }

    @Test
    void shouldCreateIngredientWhenValidRequestWithRestrictions() {
        AdmRestaurante admin = AdmRestaurante.builder().idAdmin(1).email("admin@example.com").build();
        Restricao restricao = Restricao.builder().idRestricao(1).build();
        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(restauranteRepository.findByAdmin(admin)).thenReturn(Optional.of(Restaurante.builder().build()));
        when(ingredienteRepository.existsByNomeAndAdmin("Tomate", admin)).thenReturn(false);
        when(restricaoRepository.findAllById(List.of(1))).thenReturn(List.of(restricao));
        when(ingredienteRepository.save(any(Ingrediente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = ingredienteService.cadastrarIngrediente(
                IngredienteRequestDTO.builder().nome("Tomate").restricoesIds(List.of(1)).build());

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo("Tomate");
        assertThat(response.getRestricoes()).hasSize(1);
        verify(ingredienteRestricaoRepository).saveAll(any());
    }

    @Test
    void shouldThrowWhenInvalidRestrictionIdsAreProvided() {
        AdmRestaurante admin = AdmRestaurante.builder().idAdmin(1).email("admin@example.com").build();
        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(restauranteRepository.findByAdmin(admin)).thenReturn(Optional.of(Restaurante.builder().build()));
        when(ingredienteRepository.existsByNomeAndAdmin("Tomate", admin)).thenReturn(false);
        when(restricaoRepository.findAllById(List.of(1, 2))).thenReturn(List.of(Restricao.builder().idRestricao(1).build()));

        assertThatThrownBy(() -> ingredienteService.cadastrarIngrediente(
                IngredienteRequestDTO.builder().nome("Tomate").restricoesIds(List.of(1, 2)).build()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Um ou mais IDs de restrição são inválidos.");
    }

    @Test
    void shouldListMyIngredientsWhenRestaurantExists() {
        AdmRestaurante admin = AdmRestaurante.builder().idAdmin(1).email("admin@example.com").build();
        Ingrediente ingrediente = Ingrediente.builder().idIngrediente(2).nome("Tomate").admin(admin).build();

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(restauranteRepository.findByAdmin(admin)).thenReturn(Optional.of(Restaurante.builder().build()));
        when(ingredienteRepository.findByAdmin(admin)).thenReturn(List.of(ingrediente));

        var response = ingredienteService.listarMeusIngredientes();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    void shouldThrowWhenDeletingIngredientInUse() {
        AdmRestaurante admin = AdmRestaurante.builder().idAdmin(1).email("admin@example.com").build();
        Ingrediente ingrediente = Ingrediente.builder().idIngrediente(2).nome("Tomate").admin(admin).build();

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(ingredienteRepository.findByIdIngredienteAndAdmin(2, admin)).thenReturn(Optional.of(ingrediente));
        when(refeicaoIngredienteRepository.existsByIngrediente(ingrediente)).thenReturn(true);

        assertThatThrownBy(() -> ingredienteService.deletarIngrediente(2))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Este ingrediente não pode ser deletado pois está em uso");

        verify(ingredienteRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenCreateIngredientWithoutRestaurant() {
        AdmRestaurante admin = AdmRestaurante.builder().idAdmin(1).email("admin@example.com").build();
        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(restauranteRepository.findByAdmin(admin)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.cadastrarIngrediente(
                IngredienteRequestDTO.builder().nome("Tomate").build()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Você precisa cadastrar um restaurante para ter ingredientes.");
    }

    @Test
    void shouldThrowWhenListIngredientsWithoutRestaurant() {
        AdmRestaurante admin = AdmRestaurante.builder().idAdmin(1).email("admin@example.com").build();
        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("admin@example.com");
        when(admRestauranteRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(restauranteRepository.findByAdmin(admin)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.listarMeusIngredientes())
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Você precisa cadastrar um restaurante para ter ingredientes.");
    }
}
