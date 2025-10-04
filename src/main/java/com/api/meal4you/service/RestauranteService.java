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
    private final AdmRestauranteService admRestauranteService;

    private void verificarRestauranteDoAdmLogado(Restaurante restaurante, String emailAdmLogado) {
        if (!restaurante.getAdmin().getEmail().equals(emailAdmLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não pode acessar restaurante de outro administrador");
        }
    }

    public RestauranteResponseDTO cadastrarRestaurante(RestauranteRequestDTO dto) {
        String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
        AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
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

    @Transactional
    public List<RestauranteResponseDTO> listarTodos() {
        return restauranteRepository.findAll()
                .stream()
                .map(RestauranteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestauranteResponseDTO atualizarPorAdmLogado(int id, RestauranteRequestDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Restaurante não encontrado"));

        String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
        verificarRestauranteDoAdmLogado(restaurante, emailAdmLogado);

        boolean alterado = false;

        if (dto.getNome() != null && !dto.getNome().isBlank()
                && !dto.getNome().equals(restaurante.getNome())) {
            restaurante.setNome(dto.getNome());
            alterado = true;
        }

        if (dto.getLocalizacao() != null && !dto.getLocalizacao().isBlank()
                && !dto.getLocalizacao().equals(restaurante.getLocalizacao())) {
            restaurante.setLocalizacao(dto.getLocalizacao());
            alterado = true;
        }

        if (dto.getTipo_comida() != null && !dto.getTipo_comida().isBlank()
                && !dto.getTipo_comida().equals(restaurante.getTipo_comida())) {
            restaurante.setTipo_comida(dto.getTipo_comida());
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
    }

    public void deletarRestaurante(String nome, String localizacao) {
        String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();

        Restaurante restaurante = restauranteRepository
                .findByNomeAndLocalizacao(nome, localizacao)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não existe restaurante com nome '" + nome + "' e localização '" + localizacao + "'"));

        verificarRestauranteDoAdmLogado(restaurante, emailAdmLogado);

        restauranteRepository.delete(restaurante);
    }
}
