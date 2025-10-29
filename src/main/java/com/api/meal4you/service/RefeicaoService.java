package com.api.meal4you.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.meal4you.dto.RefeicaoRequestDTO;
import com.api.meal4you.dto.RefeicaoResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.RefeicaoIngrediente;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.mapper.RefeicaoMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefeicaoService {

    private final RefeicaoRepository refeicaoRepository;
    private final AdmRestauranteService admRestauranteService;
    private final AdmRestauranteRepository admRestauranteRepository;
    private final RestauranteRepository restauranteRepository;
    private final IngredienteRepository ingredienteRepository;
    private final RefeicaoIngredienteRepository refeicaoIngredienteRepository;

    
    @Transactional
    public RefeicaoResponseDTO cadastrarRefeicao(RefeicaoRequestDTO dto) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));
            
            Restaurante restaurante = restauranteRepository.findByAdmin(adminExistente)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado. Você precisa cadastrar um restaurante primeiro."));

            if (dto.getIngredientesIds() == null || dto.getIngredientesIds().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A refeição deve ter pelo menos 1 ingrediente.");
            }

            if (refeicaoRepository.existsByNomeAndRestaurante(dto.getNome(), restaurante)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já possui uma refeição com este nome no seu cardápio.");
            }

            List<Ingrediente> ingredientes = ingredienteRepository.findAllById(dto.getIngredientesIds());

            if (ingredientes.size() != dto.getIngredientesIds().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais IDs de ingredientes são inválidos.");
            }

            for (Ingrediente ing : ingredientes) {
                if (ing.getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode adicionar ingredientes que não pertencem a você.");
                }
            }
            
            Refeicao novaRefeicao = RefeicaoMapper.toEntity(dto, restaurante);
            refeicaoRepository.save(novaRefeicao);

            List<RefeicaoIngrediente> associacoes = ingredientes.stream()
                .map(ingrediente -> RefeicaoIngrediente.builder()
                    .refeicao(novaRefeicao)
                    .ingrediente(ingrediente)
                    .build())
                .collect(Collectors.toList());
            
            refeicaoIngredienteRepository.saveAll(associacoes);
            novaRefeicao.setRefeicaoIngredientes(associacoes);

            return RefeicaoMapper.toResponse(novaRefeicao);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Erro ao cadastrar refeição: " + ex.getMessage());
        }
    }

    @Transactional
    public List<RefeicaoResponseDTO> listarMinhasRefeicoes() {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            Restaurante restaurante = restauranteRepository.findByAdmin(adminExistente)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado para este administrador."));
            
            List<Refeicao> refeicoes = refeicaoRepository.findByRestaurante(restaurante);

            return RefeicaoMapper.toResponseList(refeicoes);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Erro ao listar refeições: " + ex.getMessage());
        }
    }

    @Transactional
    public RefeicaoResponseDTO atualizarRefeicao(int id, RefeicaoRequestDTO dto) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            Refeicao refeicao = refeicaoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refeição não encontrada."));

            if (refeicao.getRestaurante().getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para alterar esta refeição.");
            }

            boolean alterado = false;

            if (dto.getNome() != null && !dto.getNome().isBlank() && !dto.getNome().equals(refeicao.getNome())) {
                if (refeicaoRepository.existsByNomeAndRestauranteAndIdRefeicaoNot(dto.getNome(), refeicao.getRestaurante(), id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já possui outra refeição com este nome no seu cardápio.");
                }
                refeicao.setNome(dto.getNome());
                alterado = true;
            }
            if (dto.getDescricao() != null && !dto.getDescricao().equals(refeicao.getDescricao())) {
                refeicao.setDescricao(dto.getDescricao());
                alterado = true;
            }
            if (dto.getTipo() != null && !dto.getTipo().isBlank() && !dto.getTipo().equals(refeicao.getTipo())) {
                refeicao.setTipo(dto.getTipo());
                alterado = true;
            }
            if (dto.getPreco() != null && dto.getPreco().compareTo(refeicao.getPreco()) != 0) {
                refeicao.setPreco(dto.getPreco());
                alterado = true;
            }

            if (dto.getIngredientesIds() != null) {
                if (dto.getIngredientesIds().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A refeição deve ter pelo menos 1 ingrediente.");
                }

                refeicaoIngredienteRepository.deleteByRefeicao(refeicao);

                List<Ingrediente> novosIngredientes = ingredienteRepository.findAllById(dto.getIngredientesIds());
                if (novosIngredientes.size() != dto.getIngredientesIds().size()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais IDs de ingredientes são inválidos.");
                }
                for (Ingrediente ing : novosIngredientes) {
                    if (ing.getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode adicionar ingredientes que não pertencem a você.");
                    }
                }
                
                List<RefeicaoIngrediente> novasAssociacoes = novosIngredientes.stream()
                    .map(ingrediente -> RefeicaoIngrediente.builder().refeicao(refeicao).ingrediente(ingrediente).build())
                    .collect(Collectors.toList());
                refeicaoIngredienteRepository.saveAll(novasAssociacoes);
                refeicao.setRefeicaoIngredientes(novasAssociacoes);
                
                alterado = true;
            }

            if (!alterado) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
            }

            refeicaoRepository.save(refeicao);
            return RefeicaoMapper.toResponse(refeicao);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao atualizar refeição: " + ex.getMessage());
        }
    }

    @Transactional
    public void deletarRefeicao(int id) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            Refeicao refeicao = refeicaoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refeição não encontrada."));

            if (refeicao.getRestaurante().getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para deletar esta refeição.");
            }

            refeicaoIngredienteRepository.deleteByRefeicao(refeicao);

            refeicaoRepository.delete(refeicao);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Erro ao deletar refeição: " + ex.getMessage());
        }
    }

    @Transactional
    public RefeicaoResponseDTO atualizarDisponibilidade(int id, boolean disponivel) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));
            
            Refeicao refeicao = refeicaoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refeição não encontrada."));

            if (refeicao.getRestaurante().getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para alterar esta refeição.");
            }

            if (refeicao.getDisponivel() == disponivel) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada. A refeição já está neste estado.");
            }

            refeicao.setDisponivel(disponivel);
            refeicaoRepository.save(refeicao);

            return RefeicaoMapper.toResponse(refeicao);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Erro ao atualizar disponibilidade: " + ex.getMessage());
        }
    }
}
