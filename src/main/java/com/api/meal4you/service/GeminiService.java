package com.api.meal4you.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;   
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash-lite:generateContent?key=%s";

    public String gerarListaDeRestricoes(String prompt) {
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = String.format(API_URL, apiKey);

        try {
            ResponseEntity<Map<String, Object>> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null)
                return "";

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                @SuppressWarnings("unchecked")
                List<Map<String, String>> parts = (List<Map<String, String>>) contentMap.get("parts");
                String text = parts.get(0).get("text");
                return text != null ? text : "";
            }
            return "";

        } catch (HttpClientErrorException e) { 
            String mensagem = String.format("Erro na comunicação com a API externa: %s", e.getStatusCode());
            throw new ResponseStatusException(e.getStatusCode(), mensagem);

        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Serviço externo indisponível. Verifique a conexão de rede.");

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao processar a resposta da API.");
        }
    }
}
