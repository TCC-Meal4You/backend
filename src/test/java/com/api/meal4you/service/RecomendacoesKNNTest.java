package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.api.meal4you.base.BaseUnitTest;
import com.api.meal4you.dto.RecomendacaoKNNResponseDTO;

class RecomendacoesKNNTest extends BaseUnitTest {

    private static final String BASE_URL = "http://recomendacoes/";

    private RecomendacoesKNN recomendacoesKNN;

    private RestTemplate restTemplate;

    private RestTemplateBuilder restTemplateBuilder;

    @BeforeEach
    void setUp() {
        restTemplate = org.mockito.Mockito.mock(RestTemplate.class);
        restTemplateBuilder = org.mockito.Mockito.mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        recomendacoesKNN = new RecomendacoesKNN(restTemplateBuilder);
        ReflectionTestUtils.setField(recomendacoesKNN, "baseUrl", BASE_URL);
    }

    @Test
    void shouldReturnMealRecommendationsWhenUserIdIsValid() {
        Integer userId = 10;
        RecomendacaoKNNResponseDTO expected = new RecomendacaoKNNResponseDTO(userId, java.util.List.of(101, 102), "OK");

        when(restTemplate.getForObject(BASE_URL + "usuarios/recomendacoes-knn/refeicoes/" + userId,
                RecomendacaoKNNResponseDTO.class)).thenReturn(expected);

        RecomendacaoKNNResponseDTO result = recomendacoesKNN.obterRecomendacaoRefeicaoKnn(userId);

        assertThat(result).isNotNull();
        assertThat(result.getIdUsuario()).isEqualTo(userId);
        assertThat(result.getRecomendacoes()).containsExactly(101, 102);
    }

    @Test
    void shouldReturnRestaurantRecommendationsWhenUserIdIsValid() {
        Integer userId = 11;
        RecomendacaoKNNResponseDTO expected = new RecomendacaoKNNResponseDTO(userId, java.util.List.of(201, 202), "OK");

        when(restTemplate.getForObject(BASE_URL + "usuarios/recomendacoes-knn/restaurantes/" + userId,
                RecomendacaoKNNResponseDTO.class)).thenReturn(expected);

        RecomendacaoKNNResponseDTO result = recomendacoesKNN.obterRecomendacaoRestauranteKnn(userId);

        assertThat(result).isNotNull();
        assertThat(result.getIdUsuario()).isEqualTo(userId);
        assertThat(result.getRecomendacoes()).containsExactly(201, 202);
    }

    @Test
    void shouldReturnNullWhenUserIdIsNull() {
        assertThat(recomendacoesKNN.obterRecomendacaoRefeicaoKnn(null)).isNull();
        assertThat(recomendacoesKNN.obterRecomendacaoRestauranteKnn(null)).isNull();
    }

    @Test
    void shouldThrowBadRequestWhenExternalServiceReturnsClientErrorForMealRecommendations() {
        Integer userId = 12;

        when(restTemplate.getForObject(BASE_URL + "usuarios/recomendacoes-knn/refeicoes/" + userId,
                RecomendacaoKNNResponseDTO.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> recomendacoesKNN.obterRecomendacaoRefeicaoKnn(userId))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Erro ao obter recomendações ou id_usuario inválido.");
    }

    @Test
    void shouldThrowInternalServerErrorWhenExternalServiceFailsForRestaurantRecommendations() {
        Integer userId = 13;

        when(restTemplate.getForObject(BASE_URL + "usuarios/recomendacoes-knn/restaurantes/" + userId,
                RecomendacaoKNNResponseDTO.class)).thenThrow(new RestClientException("Service unavailable"));

        assertThatThrownBy(() -> recomendacoesKNN.obterRecomendacaoRestauranteKnn(userId))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Erro de comunicação com o serviço de recomendações.");
    }
}
