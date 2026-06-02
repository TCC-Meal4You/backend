package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

class GeminiServiceTest {

    private GeminiService geminiService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        geminiService = new GeminiService(restTemplate);
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-key");
        ReflectionTestUtils.setField(geminiService, "apiUrl", "http://localhost/fake?key=%s");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void shouldReturnEmptyStringWhenApiReturnsNoCandidates() {
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost/fake?key=test-key"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        String result = geminiService.gerarListaDeRestricoes("prompt");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldReturnTextWhenApiReturnsCandidate() {
        String body = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"nova restrição\"}]}}]}";
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost/fake?key=test-key"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(body, MediaType.APPLICATION_JSON));

        String result = geminiService.gerarListaDeRestricoes("prompt");

        assertThat(result).isEqualTo("nova restrição");
        mockServer.verify();
    }

    @Test
    void shouldThrowWhenApiReturnsBadResponseStructure() {
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost/fake?key=test-key"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess("{\"candidates\":[{\"content\":{}}]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> geminiService.gerarListaDeRestricoes("prompt"))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Erro interno do servidor ao processar a resposta da API");
    }
}