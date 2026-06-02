package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.api.meal4you.dto.ViaCepResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

class ViaCepServiceTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private ViaCepService viaCepService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RestTemplate restTemplate = new RestTemplate();
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        viaCepService = new ViaCepService(restTemplateBuilder);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void shouldReturnNullWhenCepIsBlank() {
        assertThat(viaCepService.buscarEnderecoPorCep("  ")).isNull();
    }

    @Test
    void shouldReturnAddressWhenApiReturnsValidResponse() {
        String cep = "01001-000";
        mockServer.expect(MockRestRequestMatchers.requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(MockRestResponseCreators.withSuccess(
                        "{\"cep\":\"01001-000\",\"logradouro\":\"Praça da Sé\",\"localidade\":\"São Paulo\",\"uf\":\"SP\"}",
                        MediaType.APPLICATION_JSON));

        ViaCepResponseDTO response = viaCepService.buscarEnderecoPorCep(cep);

        assertThat(response).isNotNull();
        assertThat(response.getCep()).isEqualTo("01001-000");
        assertThat(response.getLocalidade()).isEqualTo("São Paulo");
        mockServer.verify();
    }

    @Test
    void shouldThrowBadRequestWhenApiReturnsErrorFlag() {
        String cep = "99999-999";
        mockServer.expect(MockRestRequestMatchers.requestTo("https://viacep.com.br/ws/99999999/json/"))
                .andRespond(MockRestResponseCreators.withSuccess(
                        "{\"erro\":true}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> viaCepService.buscarEnderecoPorCep(cep))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("CEP inválido ou não encontrado");
    }
}
