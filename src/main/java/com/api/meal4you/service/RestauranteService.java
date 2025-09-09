package com.api.meal4you.service;

import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.RestauranteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final AdmRestauranteRepository admRestauranteRepository;

    public void cadastrarRestaurante(Restaurante restaurante, Integer idAdmin) {
        AdmRestaurante adminExistente = admRestauranteRepository.findById(idAdmin)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin não encontrado"));

        boolean existe = restauranteRepository.findByNomeAndLocalizacao(
                restaurante.getNome(), restaurante.getLocalizacao()).isPresent();

        if (existe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Já existe um restaurante com esse nome e localização");
        }

        restaurante.setAdmin(adminExistente);
        restauranteRepository.saveAndFlush(restaurante);
    }

    public List<Restaurante> listarTodos() {
        return restauranteRepository.findAll();
    }

    @Transactional
    public void atualizarPorId(int id, Restaurante restaurante) {
        Restaurante restauranteEntity = restauranteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Restaurante não encontrado"));

        boolean alterado = false;

        if (restaurante.getNome() != null && !restaurante.getNome().isBlank()
                && !restaurante.getNome().equals(restauranteEntity.getNome())) {
            restauranteEntity.setNome(restaurante.getNome());
            alterado = true;
        }

        if (restaurante.getLocalizacao() != null && !restaurante.getLocalizacao().isBlank()
                && !restaurante.getLocalizacao().equals(restauranteEntity.getLocalizacao())) {
            restauranteEntity.setLocalizacao(restaurante.getLocalizacao());
            alterado = true;
        }

        if (restaurante.getTipo_comida() != null && !restaurante.getTipo_comida().isBlank()
                && !restaurante.getTipo_comida().equals(restauranteEntity.getTipo_comida())) {
            restauranteEntity.setTipo_comida(restaurante.getTipo_comida());
            alterado = true;
        }

        if (restauranteEntity.isAberto() != restaurante.isAberto()) {
            restauranteEntity.setAberto(restaurante.isAberto());
            alterado = true;
        }

        if (!alterado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
        }

        restauranteRepository.save(restauranteEntity);
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
