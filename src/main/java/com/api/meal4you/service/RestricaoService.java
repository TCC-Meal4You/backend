package com.api.meal4you.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.meal4you.dto.RestricaoResponseDTO;
import com.api.meal4you.dto.SincronizacaoRequestDTO;
import com.api.meal4you.entity.Restricao;
import com.api.meal4you.mapper.RestricaoMapper;
import com.api.meal4you.repository.RestricaoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestricaoService {

    private final RestricaoRepository restricaoRepository;

    @Value("${senha.secreta.sincronizacao}")
    private String senhaSecreta;

    @Transactional
    public List<RestricaoResponseDTO> listarTodas() {
        try {
            List<Restricao> restricoes = restricaoRepository.findAll();
            return RestricaoMapper.toResponseList(restricoes);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao listar restrições: " + ex.getMessage());
        }
    }

    @Transactional
    public RestricaoResponseDTO buscarPorId(int id) {
        try {
            Restricao restricao = restricaoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Restrição não encontrada para o ID: " + id));
            return RestricaoMapper.toResponse(restricao);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao encontrar restrição: " + ex.getMessage());
        }
    }

    // Para o Controller (com senha)
    public String sincronizarComIA(SincronizacaoRequestDTO dto) {
        try {
            if (dto.getSenha() == null || !dto.getSenha().equals(senhaSecreta)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha de sincronização inválida!");
            }
            return executarLogicaDeSincronizacao();
        } catch (ResponseStatusException ex) {
            throw ex;
        }
    }

    //Para o Robô Scheduler(sem senha)
    public String sincronizarComIA() {
        return executarLogicaDeSincronizacao();
    }

    @Transactional
    private String executarLogicaDeSincronizacao() {
        // ... aqui entraria a chamada para a API do Gemini ...
        return "Sincronização com IA acionada. (Implementação pendente)";
    }
}

