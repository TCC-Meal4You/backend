package com.api.meal4you.service;

import com.api.meal4you.dto.RestauranteFavoritoResponseDTO;
import com.api.meal4you.dto.RestaurantePorIdResponseDTO;
import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.dto.UsuarioAvaliaResponseDTO;
import com.api.meal4you.entity.*;
import com.api.meal4you.mapper.RestauranteMapper;
import com.api.meal4you.repository.*;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.RestauranteFavorito;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.mapper.RestauranteMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.IngredienteRestricaoRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteFavoritoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final AdmRestauranteRepository admRestauranteRepository;
    private final AdmRestauranteService admRestauranteService;
    private final IngredienteRepository ingredienteRepository;
    private final IngredienteRestricaoRepository ingredienteRestricaoRepository;
    private final RefeicaoRepository refeicaoRepository;
    private final RefeicaoIngredienteRepository refeicaoIngredienteRepository;
    private final UsuarioAvaliaRepository usuarioAvaliaRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteFavoritoRepository restauranteFavoritoRepository;

    private void verificarRestauranteDoAdmLogado(Restaurante restaurante, String emailAdmLogado) {
        if (!restaurante.getAdmin().getEmail().equals(emailAdmLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não pode acessar restaurante de outro administrador");
        }
    }

    @Transactional
    public RestauranteResponseDTO cadastrarRestaurante(RestauranteRequestDTO dto) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Administrador não autenticado."));

            boolean existe = restauranteRepository.findByNomeAndLocalizacao(dto.getNome(), dto.getLocalizacao())
                    .isPresent();
            if (existe) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Já existe um restaurante com esse nome e localização");
            }

            Restaurante restaurante = RestauranteMapper.toEntity(dto, adminExistente);
            restauranteRepository.saveAndFlush(restaurante);

            return RestauranteMapper.toResponse(restaurante);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao cadastrar restaurante: " + ex.getMessage());
        }
    }

    @Transactional
    public List<RestauranteFavoritoResponseDTO> listarTodos() {
        try {
            List<Restaurante> restaurantes = restauranteRepository.findAll();
            
            String emailLogado = usuarioService.getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));
            
            List<RestauranteFavorito> favoritos = restauranteFavoritoRepository.findByUsuario(usuario);
            
            Set<Integer> idsFavoritados = favoritos.stream()
                    .map(fav -> fav.getRestaurante().getIdRestaurante())
                    .collect(Collectors.toSet());
            
            return RestauranteMapper.toUsuarioCardResponseList(restaurantes, idsFavoritados);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao listar restaurantes: " + ex.getMessage());
        }
    }

    @Transactional
    public RestauranteResponseDTO atualizarPorAdmLogado(int id, RestauranteRequestDTO dto) {
        try {
            Restaurante restaurante = restauranteRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado"));

            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            verificarRestauranteDoAdmLogado(restaurante, emailAdmLogado);

            boolean alterado = false;

            if (dto.getNome() != null && !dto.getNome().isBlank() && !dto.getNome().equals(restaurante.getNome())) {
                restaurante.setNome(dto.getNome());
                alterado = true;
            }

            if (dto.getLocalizacao() != null && !dto.getLocalizacao().isBlank()
                    && !dto.getLocalizacao().equals(restaurante.getLocalizacao())) {
                restaurante.setLocalizacao(dto.getLocalizacao());
                alterado = true;
            }

            if (dto.getDescricao() != null && !dto.getDescricao().isBlank()
                    && !dto.getDescricao().equals(restaurante.getDescricao())) {
                restaurante.setDescricao(dto.getDescricao());
                alterado = true;
            }

            if (dto.getTipoComida() != null && !dto.getTipoComida().isBlank()
                    && !dto.getTipoComida().equals(restaurante.getTipoComida())) {
                restaurante.setTipoComida(dto.getTipoComida());
                alterado = true;
            }

            if (restaurante.isAtivo() != dto.isAtivo()) {
                restaurante.setAtivo(dto.isAtivo());
                alterado = true;
            }

            if (!alterado) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
            }

            restauranteRepository.save(restaurante);
            return RestauranteMapper.toResponse(restaurante);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar restaurante: " + ex.getMessage());
        }
    }

    @Transactional
    public void deletarRestaurante(int id, String nomeConfirmacao) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante admin = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Administrador não autenticado."));

            Restaurante restaurante = restauranteRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado"));

            verificarRestauranteDoAdmLogado(restaurante, emailAdmLogado);

            if (!restaurante.getNome().equals(nomeConfirmacao)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Nome de confirmação do restaurante incorreto.");
            }

            List<Refeicao> refeicoes = refeicaoRepository.findByRestaurante(restaurante);
            if (!refeicoes.isEmpty()) {
                refeicoes.forEach(refeicaoIngredienteRepository::deleteByRefeicao);
                refeicaoRepository.deleteAll(refeicoes);
            }

            List<Ingrediente> ingredientes = ingredienteRepository.findByAdmin(admin);
            if (!ingredientes.isEmpty()) {
                ingredientes.forEach(ingredienteRestricaoRepository::deleteByIngrediente);
                ingredienteRepository.deleteAll(ingredientes);
            }

            usuarioAvaliaRepository.deleteByRestaurante(restaurante);

            restauranteFavoritoRepository.deleteByRestaurante(restaurante);
            restauranteRepository.delete(restaurante);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao deletar restaurante: " + ex.getMessage());
        }
    }

    @Transactional
    public RestauranteResponseDTO buscarMeuRestaurante() {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();

            AdmRestaurante admin = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado"));

            Restaurante restaurante = restauranteRepository.findByAdmin(admin)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Restaurante não encontrado. Cadastre primeiro"));

            return RestauranteMapper.toResponse(restaurante);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar restaurante: " + ex.getMessage());
        }
    }

    @Transactional
    public RestaurantePorIdResponseDTO listarPorId(int id, Integer numPagina) {
        try {
            Restaurante restaurante = restauranteRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado"));

            List<Refeicao> todasRefeicoes = refeicaoRepository.findByRestaurante(restaurante);

            List<Refeicao> refeicoesDisponiveis = todasRefeicoes.stream()
                    .filter(Refeicao::getDisponivel)
                    .collect(Collectors.toList());

            int tamanhoPagina = 10;
            int pagina = (numPagina != null && numPagina > 0) ? numPagina : 1;
            int inicio = (pagina - 1) * tamanhoPagina;
            int fim = Math.min(inicio + tamanhoPagina, refeicoesDisponiveis.size());

            int totalRefeicoesDisponiveis = refeicoesDisponiveis.size();

            int totalPaginas = (int) Math.ceil((double) totalRefeicoesDisponiveis / tamanhoPagina);
            if (totalPaginas == 0 && totalRefeicoesDisponiveis > 0) {
                totalPaginas = 1;
            } else if (totalRefeicoesDisponiveis == 0) {
                totalPaginas = 0;
            }

            List<Refeicao> refeicoesPaginadas = refeicoesDisponiveis.subList(inicio, fim);

            String emailLogado = usuarioService.getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));
            
            Optional<RestauranteFavorito> favorito = restauranteFavoritoRepository.findByUsuarioAndRestaurante(usuario, restaurante);
            boolean isFavorito = favorito.isPresent();

            return RestauranteMapper.toPorIdResponse(restaurante, refeicoesPaginadas, totalPaginas, isFavorito);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar restaurante por ID: " + ex.getMessage());
        }
    }

   @Transactional
    public List<UsuarioAvaliaResponseDTO> listarAvaliacoesDoMeuRestaurante() {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();

            AdmRestaurante admin = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Administrador não encontrado"));

            Restaurante restaurante = restauranteRepository.findByAdmin(admin)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Restaurante não encontrado. Cadastre primeiro"));

            List<com.api.meal4you.entity.UsuarioAvalia> avaliacoes = usuarioAvaliaRepository.findByRestaurante(restaurante);

            return avaliacoes.stream().map(com.api.meal4you.mapper.UsuarioAvaliaMapper::toResponse).collect(java.util.stream.Collectors.toList());

        } catch (org.springframework.web.server.ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao listar avaliações do restaurante: " + ex.getMessage());
        }
    }
  
    public void alternarFavorito(int idRestaurante) {
        try {
            String emailLogado = usuarioService.getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

            Restaurante restaurante = restauranteRepository.findById(idRestaurante)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado."));

            Optional<RestauranteFavorito> favoritoExistente = restauranteFavoritoRepository.findByUsuarioAndRestaurante(usuario, restaurante);

            if (favoritoExistente.isPresent()) {
                restauranteFavoritoRepository.delete(favoritoExistente.get());
            } else {
                RestauranteFavorito novoFavorito = RestauranteMapper.toEntity(usuario, restaurante);
                restauranteFavoritoRepository.save(novoFavorito);
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao alternar favorito.", ex);
        }
    }

    
    @Transactional
    public List<RestauranteFavoritoResponseDTO> listarRestaurantesFavoritos() {
        try {
            String emailLogado = usuarioService.getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

            List<RestauranteFavorito> favoritos = restauranteFavoritoRepository.findByUsuario(usuario);

            List<Restaurante> restaurantes = favoritos.stream()
                    .map(RestauranteFavorito::getRestaurante)
                    .collect(Collectors.toList());

            Set<Integer> idsFavoritados = restaurantes.stream()
                    .map(Restaurante::getIdRestaurante)
                    .collect(Collectors.toSet());

            return RestauranteMapper.toUsuarioCardResponseList(restaurantes, idsFavoritados);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao listar favoritos.", ex);
        }
    }
}
