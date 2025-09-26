package com.api.meal4you.service;

import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.mapper.RestauranteMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.RestauranteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final AdmRestauranteRepository admRestauranteRepository;

    //Depois para o cadastro fazer com que pegue pelo id do adm logado
    public RestauranteResponseDTO cadastrarRestaurante(RestauranteRequestDTO dto, Integer idAdmin) {
        AdmRestaurante adminExistente = admRestauranteRepository.findById(idAdmin)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin não encontrado"));

        boolean existe = restauranteRepository.findByNomeAndLocalizacao(
                dto.getNome(), dto.getLocalizacao()).isPresent();

        if (existe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Já existe um restaurante com esse nome e localização");
        }

        Restaurante restaurante = RestauranteMapper.toEntity(dto, adminExistente);
        restauranteRepository.saveAndFlush(restaurante);

        return RestauranteMapper.toResponse(restaurante);
    }

    public List<RestauranteResponseDTO> listarTodos() {
        return restauranteRepository.findAll()
            .stream()
            .map(RestauranteMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public RestauranteResponseDTO atualizarPorId(int id, RestauranteRequestDTO dto) {
        Restaurante restauranteEntity = restauranteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Restaurante não encontrado"));

        boolean alterado = false;

        if (dto.getNome() != null && !dto.getNome().isBlank()
                && !dto.getNome().equals(restauranteEntity.getNome())) {
            restauranteEntity.setNome(dto.getNome());
            alterado = true;
        }

        if (dto.getLocalizacao() != null && !dto.getLocalizacao().isBlank()
                && !dto.getLocalizacao().equals(restauranteEntity.getLocalizacao())) {
            restauranteEntity.setLocalizacao(dto.getLocalizacao());
            alterado = true;
        }

        if (dto.getTipo_comida() != null && !dto.getTipo_comida().isBlank()
                && !dto.getTipo_comida().equals(restauranteEntity.getTipo_comida())) {
            restauranteEntity.setTipo_comida(dto.getTipo_comida());
            alterado = true;
        }

        if (restauranteEntity.isAberto() != dto.isAberto()) {
            restauranteEntity.setAberto(dto.isAberto());
            alterado = true;
        }

        if (!alterado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
        }

        restauranteRepository.save(restauranteEntity);
        return RestauranteMapper.toResponse(restauranteEntity);
    }

    public void deletarRestaurante(String nome, String localizacao) {
        Restaurante restaurante = restauranteRepository
                .findByNomeAndLocalizacao(nome, localizacao)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não existe restaurante com nome '" + nome + "' e localização '" + localizacao + "'"));

        restauranteRepository.delete(restaurante);
    }
}
