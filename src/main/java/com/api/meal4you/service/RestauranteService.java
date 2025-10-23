package com.api.meal4you.service;

import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.mapper.RestauranteMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.RestauranteRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final AdmRestauranteRepository admRestauranteRepository;
    private final AdmRestauranteService admRestauranteService;

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
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado"));

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
    public void deletarRestaurante(String nome, String localizacao) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();

            Restaurante restaurante = restauranteRepository
                    .findByNomeAndLocalizacao(nome, localizacao)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Não existe restaurante com nome '" + nome + "' e localização '" + localizacao + "'"));

            verificarRestauranteDoAdmLogado(restaurante, emailAdmLogado);

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
}
