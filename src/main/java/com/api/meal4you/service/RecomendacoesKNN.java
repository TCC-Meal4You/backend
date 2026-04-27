package com.api.meal4you.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.api.meal4you.dto.RecomendacaoKNNResponseDTO;

@Service
public class RecomendacoesKNN {

    private final RestTemplate restTemplate;

    @Value("${url.servico.recomendacoes}")
    private String baseUrl;

    public RecomendacoesKNN(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public RecomendacaoKNNResponseDTO obterRecomendacaoRefeicaoKnn(Integer idUsuario) {
        if (idUsuario == null) {
            return null;
        }

        String url = baseUrl + "usuarios/recomendacoes-knn/refeicoes/" + idUsuario;

        try {
            RecomendacaoKNNResponseDTO response = restTemplate.getForObject(url, RecomendacaoKNNResponseDTO.class);
            return response;
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao obter recomendações ou id_usuario inválido.");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro de comunicação com o serviço de recomendações.");
        }
    }

    public RecomendacaoKNNResponseDTO obterRecomendacaoRestauranteKnn(Integer idUsuario) {
        if (idUsuario == null) {
            return null;
        }

        String url = baseUrl + "usuarios/recomendacoes-knn/restaurantes/" + idUsuario;

        try {
            RecomendacaoKNNResponseDTO response = restTemplate.getForObject(url, RecomendacaoKNNResponseDTO.class);
            return response;
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao obter recomendações ou id_usuario inválido.");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro de comunicação com o serviço de recomendações.");
        }
    }
}
