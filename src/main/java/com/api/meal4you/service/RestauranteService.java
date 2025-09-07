package com.api.meal4you.service;

import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.RestauranteRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final AdmRestauranteRepository admRestauranteRepository;

    public void cadastrarRestaurante(Restaurante restaurante, Integer idAdmin) {
        AdmRestaurante adminExistente = admRestauranteRepository.findById(idAdmin)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

        boolean existe = restauranteRepository.findByNomeAndLocalizacao(
                restaurante.getNome(), restaurante.getLocalizacao()).isPresent();

        if (existe) {
            throw new RuntimeException("Já existe um restaurante com esse nome e localização");
        }

        restaurante.setAdmin(adminExistente);
        restauranteRepository.saveAndFlush(restaurante);
    }

    public List<Restaurante> listarTodos() {
        return restauranteRepository.findAll();
    }

    public void atualizarPorId(int id, Restaurante restaurante) {
        Restaurante restauranteEntity = restauranteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));

        if (restaurante.getNome() != null)
            restauranteEntity.setNome(restaurante.getNome());
        if (restaurante.getLocalizacao() != null)
            restauranteEntity.setLocalizacao(restaurante.getLocalizacao());
        if (restaurante.getTipo_comida() != null)
            restauranteEntity.setTipo_comida(restaurante.getTipo_comida());
        restauranteEntity.setAberto(restaurante.isAberto());
        
        restauranteRepository.saveAndFlush(restauranteEntity);
    }

    public void deletarRestaurante(String nome, String localizacao) {
        Restaurante restaurante = restauranteRepository
                .findByNomeAndLocalizacao(nome, localizacao)
                .orElseThrow(() -> new RuntimeException(
                        "Não existe restaurante com nome '" + nome + "' e localização '" + localizacao + "'"));

        restauranteRepository.delete(restaurante);
    }
}
