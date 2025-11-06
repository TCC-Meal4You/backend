package com.api.meal4you.service;

import com.api.meal4you.dto.RestaurantePorIdResponseDTO;
import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.dto.UsuarioAvaliaResponseDTO;
import com.api.meal4you.entity.*;
import com.api.meal4you.mapper.RestauranteMapper;
import com.api.meal4you.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
    public List<RestauranteResponseDTO> listarTodos() {
        try {
            List<Restaurante> restaurantes = restauranteRepository.findAll();
            return RestauranteMapper.toResponseList(restaurantes);
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

            if (restaurante.isAberto() != dto.isAberto()) {
                restaurante.setAberto(dto.isAberto());
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

            return RestauranteMapper.toPorIdResponse(restaurante, refeicoesPaginadas, totalPaginas);

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
}
