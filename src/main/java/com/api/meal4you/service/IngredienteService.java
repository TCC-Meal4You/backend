package com.api.meal4you.service;

import com.api.meal4you.dto.IngredienteRequestDTO;
import com.api.meal4you.dto.IngredienteResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.IngredienteRestricao;
import com.api.meal4you.entity.Restricao;
import com.api.meal4you.mapper.IngredienteMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.IngredienteRestricaoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.RestricaoRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredienteService {

    private final AdmRestauranteService admRestauranteService;
    private final AdmRestauranteRepository admRestauranteRepository;
    private final IngredienteRepository ingredienteRepository;
    private final IngredienteRestricaoRepository ingredienteRestricaoRepository;
    private final RestricaoRepository restricaoRepository;
    private final RestauranteRepository restauranteRepository;

    @Transactional
    public IngredienteResponseDTO cadastrarIngrediente(IngredienteRequestDTO dto) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado"));

            if (restauranteRepository.findByAdmin(adminExistente).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você precisa cadastrar um restaurante para ter ingredientes.");
            }

            if (ingredienteRepository.existsByNomeAndAdmin(dto.getNome(), adminExistente)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já cadastrou um ingrediente com este nome.");
            }

            Ingrediente novoIngrediente = IngredienteMapper.toEntity(dto);
            novoIngrediente.setAdmin(adminExistente);
            ingredienteRepository.save(novoIngrediente);

            if (dto.getRestricoesIds() != null && !dto.getRestricoesIds().isEmpty()) {
                List<Restricao> restricoes = restricaoRepository.findAllById(dto.getRestricoesIds());
                
                if (restricoes.size() != dto.getRestricoesIds().size()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais IDs de restrição são inválidos.");
                }

                List<IngredienteRestricao> associacoes = restricoes.stream()
                        .map(restricao -> IngredienteRestricao.builder()
                                .ingrediente(novoIngrediente)
                                .restricao(restricao)
                                .build())
                        .collect(Collectors.toList());
                
                ingredienteRestricaoRepository.saveAll(associacoes);
                novoIngrediente.setRestricoes(associacoes); 
            }

            return IngredienteMapper.toResponse(novoIngrediente);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao cadastrar ingrediente: " + ex.getMessage());
        }
    }

    @Transactional
    public List<IngredienteResponseDTO> listarMeusIngredientes() {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado"));

            if (restauranteRepository.findByAdmin(adminExistente).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você precisa cadastrar um restaurante para ter ingredientes.");
            }

            List<Ingrediente> ingredientes = ingredienteRepository.findByAdmin(adminExistente);
            return IngredienteMapper.toResponseList(ingredientes);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao listar ingredientes: " + ex.getMessage());
        }
    }

    @Transactional
    public void deletarIngrediente(int id) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado"));

            Ingrediente ingrediente = ingredienteRepository.findByIdIngredienteAndAdmin(id, adminExistente)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingrediente não encontrado ou não pertence a você."));

            ingredienteRestricaoRepository.deleteByIngrediente(ingrediente);

            ingredienteRepository.delete(ingrediente);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao deletar ingrediente. Verifique se ele não está em uso em uma refeição.");
        }
    }
}
