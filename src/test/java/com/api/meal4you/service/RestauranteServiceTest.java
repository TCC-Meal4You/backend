package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.server.ResponseStatusException;

import com.api.meal4you.base.BaseUnitTest;
import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.ViaCepResponseDTO;
import com.api.meal4you.dto.PesquisarRestauranteComFiltroRequestDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.RestauranteFavorito;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.entity.UsuarioAvalia;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteFavoritoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.UsuarioAvaliaRepository;
import com.api.meal4you.repository.UsuarioRepository;

class RestauranteServiceTest extends BaseUnitTest {

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private AdmRestauranteRepository admRestauranteRepository;

    @Mock
    private AdmRestauranteService admRestauranteService;

    @Mock
    private ViaCepService viaCepService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RestauranteFavoritoRepository restauranteFavoritoRepository;

    @Mock
    private RefeicaoRepository refeicaoRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private UsuarioAvaliaRepository usuarioAvaliaRepository;

    @InjectMocks
    private RestauranteService restauranteService;

    @AfterEach
    void tearDown() {
        // limpar contexto caso necessário
    }

    @Test
    void shouldThrowWhenAdminNotAuthenticated() {
        RestauranteRequestDTO dto = RestauranteRequestDTO.builder()
                .nome("R1")
                .cep("01001000")
                .logradouro("Rua Teste")
                .numero(10)
                .bairro("Bairro")
                .cidade("Cidade")
                .uf("SP")
                .build();

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@naoexiste.com");
        when(admRestauranteRepository.findByEmail("adm@naoexiste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restauranteService.cadastrarRestaurante(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Administrador não autenticado.");
    }

    @Test
    void shouldThrowWhenCepDataMismatch() {
        RestauranteRequestDTO dto = RestauranteRequestDTO.builder()
                .nome("R1")
                .cep("01001000")
                .logradouro("Rua Invalida")
                .numero(10)
                .bairro("Bairro")
                .cidade("Cidade")
                .uf("SP")
                .build();

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail("adm@example.com");

        ViaCepResponseDTO viaCep = ViaCepResponseDTO.builder()
                .cep("01001000")
                .logradouro("Praça Teste")
                .bairro("Outro")
                .localidade("CidadeAlterada")
                .uf("SP")
                .build();

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
        when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
        when(viaCepService.buscarEnderecoPorCep("01001000")).thenReturn(viaCep);

        assertThatThrownBy(() -> restauranteService.cadastrarRestaurante(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Os dados do endereço não correspondem ao CEP informado");
    }

    @Test
    void shouldThrowWhenRestaurantAlreadyExists() {
        RestauranteRequestDTO dto = RestauranteRequestDTO.builder()
                .nome("R1")
                .cep("01001000")
                .logradouro("Rua Teste")
                .numero(10)
                .bairro("Bairro")
                .cidade("Cidade")
                .uf("SP")
                .build();

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail("adm@example.com");

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
        when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
        when(viaCepService.buscarEnderecoPorCep("01001000")).thenReturn(null);
        when(restauranteRepository.findByNomeAndCepAndNumero(dto.getNome(), dto.getCep(), dto.getNumero()))
                .thenReturn(Optional.of(new Restaurante()));

        assertThatThrownBy(() -> restauranteService.cadastrarRestaurante(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Já existe um restaurante com esse nome e localização");
    }

    @Test
    void shouldRegisterRestaurantWhenValid() {
        RestauranteRequestDTO dto = RestauranteRequestDTO.builder()
                .nome("R1")
                .cep("01001000")
                .logradouro("Rua Teste")
                .numero(10)
                .bairro("Bairro")
                .cidade("Cidade")
                .uf("SP")
                .build();

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail("adm@example.com");

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn("adm@example.com");
        when(admRestauranteRepository.findByEmail("adm@example.com")).thenReturn(Optional.of(adm));
        when(viaCepService.buscarEnderecoPorCep("01001000")).thenReturn(null);
        when(restauranteRepository.findByNomeAndCepAndNumero(dto.getNome(), dto.getCep(), dto.getNumero()))
                .thenReturn(Optional.empty());
        when(restauranteRepository.saveAndFlush(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = restauranteService.cadastrarRestaurante(dto);

        assertThat(response).isNotNull();
        verify(restauranteRepository).saveAndFlush(any(Restaurante.class));
    }

    @Test
    void shouldListRestaurantsWhenUserAuthenticated() {
        String emailLogado = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Restaurante r1 = new Restaurante();
        r1.setIdRestaurante(1);
        r1.setNome("R1");
        r1.setAtivo(true);
        Restaurante r2 = new Restaurante();
        r2.setIdRestaurante(2);
        r2.setNome("R2");
        r2.setAtivo(true);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(emailLogado);
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findAllByAtivoTrue()).thenReturn(List.of(r1, r2));
        when(restauranteFavoritoRepository.findByUsuario(usuario)).thenReturn(List.of());

        var response = restauranteService.listarTodos(1);

        assertThat(response).isNotNull();
        assertThat(response.getTotalPaginas()).isEqualTo(1);
    }

    @Test
    void shouldUpdateRestaurantWhenValidUpdateRequest() {
        String emailAdmLogado = "adm@example.com";
        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);
        restaurante.setNome("OldName");
        restaurante.setCep("01001000");
        restaurante.setLogradouro("Rua Teste");
        restaurante.setBairro("Bairro");
        restaurante.setCidade("Cidade");
        restaurante.setUf("SP");
        restaurante.setNumero(10);
        restaurante.setAtivo(true);

        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailAdmLogado);
        restaurante.setAdmin(adm);

        RestauranteRequestDTO dto = RestauranteRequestDTO.builder()
                .nome("NewName")
                .cep("02002000")
                .logradouro("Rua Nova")
                .numero(15)
                .bairro("Novo")
                .cidade("NovaCidade")
                .uf("SP")
                .descricao("Descricao")
                .tipoComida("Italiana")
                .ativo(false)
                .build();

        ViaCepResponseDTO viaCep = ViaCepResponseDTO.builder()
                .cep("02002000")
                .logradouro("Rua Nova")
                .bairro("Novo")
                .localidade("NovaCidade")
                .uf("SP")
                .build();

        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(viaCepService.buscarEnderecoPorCep("02002000")).thenReturn(viaCep);

        var response = restauranteService.atualizarPorAdmLogado(1, dto);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo("NewName");
        verify(restauranteRepository).save(restaurante);
    }

    @Test
    void shouldDeleteRestaurantWhenConfirmationNameMatches() {
        String emailAdmLogado = "adm@example.com";
        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);
        restaurante.setNome("R1");
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailAdmLogado);
        restaurante.setAdmin(adm);

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(refeicaoRepository.findByRestaurante(restaurante)).thenReturn(List.of());
        when(ingredienteRepository.findByAdmin(adm)).thenReturn(List.of());

        restauranteService.deletarRestaurante(1, "R1");

        verify(restauranteRepository).delete(restaurante);
    }

    @Test
    void shouldFetchMyRestaurantWhenExists() {
        String emailAdmLogado = "adm@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailAdmLogado);
        Restaurante restaurante = new Restaurante();
        restaurante.setAdmin(adm);

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.of(restaurante));

        var response = restauranteService.buscarMeuRestaurante();

        assertThat(response).isNotNull();
        verify(restauranteRepository).findByAdmin(adm);
    }

    @Test
    void shouldToggleFavoriteWhenNotAlreadyFavorited() {
        String emailLogado = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(emailLogado);
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(restauranteFavoritoRepository.findByUsuarioAndRestaurante(usuario, restaurante)).thenReturn(Optional.empty());

        restauranteService.alternarFavorito(1);

        verify(restauranteFavoritoRepository).save(any());
    }

    @Test
    void shouldListFavoriteRestaurants() {
        String emailLogado = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);

        RestauranteFavorito favorito = new RestauranteFavorito();
        favorito.setUsuario(usuario);
        favorito.setRestaurante(restaurante);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(emailLogado);
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restauranteFavoritoRepository.findByUsuario(usuario)).thenReturn(List.of(favorito));

        var response = restauranteService.listarRestaurantesFavoritos();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    void shouldRemoveFavoriteWhenAlreadyFavorited() {
        String emailLogado = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);
        RestauranteFavorito favorito = new RestauranteFavorito();
        favorito.setUsuario(usuario);
        favorito.setRestaurante(restaurante);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(emailLogado);
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(restauranteFavoritoRepository.findByUsuarioAndRestaurante(usuario, restaurante)).thenReturn(Optional.of(favorito));

        restauranteService.alternarFavorito(1);

        verify(restauranteFavoritoRepository).delete(favorito);
    }

    @Test
    void shouldThrowWhenDeleteRestaurantConfirmationNameMismatch() {
        String emailAdmLogado = "adm@example.com";
        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);
        restaurante.setNome("R1");
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailAdmLogado);
        restaurante.setAdmin(adm);

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));

        assertThatThrownBy(() -> restauranteService.deletarRestaurante(1, "WrongName"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nome de confirmação do restaurante incorreto.");
    }

    @Test
    void shouldThrowWhenMyRestaurantDoesNotExist() {
        String emailAdmLogado = "adm@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailAdmLogado);

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findByAdmin(adm)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restauranteService.buscarMeuRestaurante())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Restaurante não encontrado");
    }

    @Test
    void shouldListRestaurantByIdWhenExists() {
        String emailLogado = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);
        restaurante.setNome("R1");

        Refeicao r1 = new Refeicao();
        r1.setDisponivel(true);
        r1.setRestaurante(restaurante);
        r1.setRefeicaoIngredientes(List.of());
        Refeicao r2 = new Refeicao();
        r2.setDisponivel(false);
        r2.setRestaurante(restaurante);
        r2.setRefeicaoIngredientes(List.of());

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(emailLogado);
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(refeicaoRepository.findByRestaurante(restaurante)).thenReturn(List.of(r1, r2));
        when(restauranteFavoritoRepository.findByUsuarioAndRestaurante(usuario, restaurante))
                .thenReturn(Optional.of(new RestauranteFavorito()));

        var response = restauranteService.listarPorId(1, null);

        assertThat(response).isNotNull();
        assertThat(response.isFavorito()).isTrue();
        assertThat(response.getTotalPaginas()).isEqualTo(1);
    }

    @Test
    void shouldSearchRestaurantsWithFilter() {
        String emailLogado = "user@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(emailLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);
        restaurante.setNome("Pizza Place");
        restaurante.setDescricao("Melhor pizza");
        restaurante.setTipoComida("Italiana");
        restaurante.setAtivo(true);

        when(usuarioService.getUsuarioLogadoEmail()).thenReturn(emailLogado);
        when(usuarioRepository.findByEmail(emailLogado)).thenReturn(Optional.of(usuario));
        when(restauranteRepository.findAllByAtivoTrue()).thenReturn(List.of(restaurante));
        when(restauranteFavoritoRepository.findByUsuario(usuario)).thenReturn(List.of());

        PesquisarRestauranteComFiltroRequestDTO dto = new PesquisarRestauranteComFiltroRequestDTO();
        dto.setNomeOuDescricao("pizza");
        dto.setTipoComida("italiana");

        var result = restauranteService.pesquisarComFiltro(dto, 1);

        assertThat(result).isNotNull();
        assertThat(result.getTotalPaginas()).isEqualTo(1);
    }

    @Test
    void shouldListReviewsByRestaurantId() {
        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);

        UsuarioAvalia avaliacao = new UsuarioAvalia();
        avaliacao.setRestaurante(restaurante);
        avaliacao.setUsuario(new Usuario());
        avaliacao.setNota(4);
        avaliacao.setComentario("Bom");

        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(usuarioAvaliaRepository.findByRestaurante(restaurante)).thenReturn(List.of(avaliacao));

        var response = restauranteService.listarAvaliacoesPorIdDoRestaurante(1);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    void shouldCreateRestauranteSuccessfully() {
        String emailAdmLogado = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailAdmLogado);

        RestauranteRequestDTO dto = RestauranteRequestDTO.builder()
                .nome("Novo Restaurante")
                .cep("01001000")
                .bairro("Centro")
                .logradouro("Rua Teste")
                .cidade("São Paulo")
                .uf("SP")
                .numero(100)
                .build();

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(viaCepService.buscarEnderecoPorCep(dto.getCep())).thenReturn(new ViaCepResponseDTO(dto.getCep(), dto.getLogradouro(), dto.getBairro(), dto.getCidade(), dto.getUf()));
        when(restauranteRepository.findByNomeAndCepAndNumero(dto.getNome(), dto.getCep(), dto.getNumero())).thenReturn(Optional.empty());
        when(restauranteRepository.saveAndFlush(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = restauranteService.cadastrarRestaurante(dto);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo(dto.getNome());
    }

    @Test
    void shouldUpdateRestauranteWithValidChanges() {
        String emailAdmLogado = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailAdmLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);
        restaurante.setAdmin(adm);
        restaurante.setNome("Old Name");
        restaurante.setCep("01001000");
        restaurante.setLogradouro("Rua Teste");
        restaurante.setBairro("Centro");
        restaurante.setCidade("São Paulo");
        restaurante.setUf("SP");
        restaurante.setNumero(100);

        RestauranteRequestDTO dto = RestauranteRequestDTO.builder()
                .nome("New Name")
                .cep("01001000")
                .logradouro("Rua Teste")
                .bairro("Centro")
                .cidade("São Paulo")
                .uf("SP")
                .numero(100)
                .build();

        org.mockito.Mockito.lenient().when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        org.mockito.Mockito.lenient().when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        org.mockito.Mockito.lenient().when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        org.mockito.Mockito.lenient().when(viaCepService.buscarEnderecoPorCep(dto.getCep())).thenReturn(new ViaCepResponseDTO(dto.getCep(), dto.getLogradouro(), dto.getBairro(), dto.getCidade(), dto.getUf()));

        var response = restauranteService.atualizarPorAdmLogado(1, dto);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo("New Name");
    }

    @Test
    void shouldDeleteRestauranteWhenConfirmationNameMatches() {
        String emailAdmLogado = "admin@example.com";
        AdmRestaurante adm = new AdmRestaurante();
        adm.setEmail(emailAdmLogado);

        Restaurante restaurante = new Restaurante();
        restaurante.setIdRestaurante(1);
        restaurante.setNome("Restaurante Teste");
        restaurante.setAdmin(adm);

        when(admRestauranteService.getAdmLogadoEmail()).thenReturn(emailAdmLogado);
        when(admRestauranteRepository.findByEmail(emailAdmLogado)).thenReturn(Optional.of(adm));
        when(restauranteRepository.findById(1)).thenReturn(Optional.of(restaurante));
        when(refeicaoRepository.findByRestaurante(restaurante)).thenReturn(List.of());
        when(ingredienteRepository.findByAdmin(adm)).thenReturn(List.of());

        restauranteService.deletarRestaurante(1, "Restaurante Teste");

        verify(restauranteRepository).delete(restaurante);
    }
}
