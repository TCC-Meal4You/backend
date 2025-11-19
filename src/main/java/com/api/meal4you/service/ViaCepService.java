package com.api.meal4you.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;

import com.api.meal4you.dto.ViaCepResponseDTO;

@Service
public class ViaCepService {
    private final RestTemplate restTemplate;

        public ViaCepService(RestTemplateBuilder restTemplateBuilder) {
            this.restTemplate = restTemplateBuilder.build();
        }

       public ViaCepResponseDTO buscarEnderecoPorCep(String cep) {
        if (cep == null || cep.isBlank()) {
            return null;
        }
        String url = "https://viacep.com.br/ws/" + cep.replaceAll("[^0-9]", "") + "/json/";
        try {
            ViaCepResponseDTO response = restTemplate.getForObject(url, ViaCepResponseDTO.class);
            if (response != null && response.isErro()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP inválido ou não encontrado.");
            }
            return response;
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP inválido ou não encontrado.");
        }
    }
}
