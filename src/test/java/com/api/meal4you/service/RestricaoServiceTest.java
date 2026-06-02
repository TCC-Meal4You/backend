package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.api.meal4you.dto.RestricaoResponseDTO;
import com.api.meal4you.dto.SincronizacaoRequestDTO;
import com.api.meal4you.entity.Restricao;
import com.api.meal4you.repository.RestricaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class RestricaoServiceTest {

    @Mock
    private RestricaoRepository restricaoRepository;

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private RestricaoService restricaoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(restricaoService, "senhaSecreta", "test-secret-key");
    }

    @Test
    void shouldListAllRestrictions() {
        when(restricaoRepository.findAll()).thenReturn(List.of(
                Restricao.builder().idRestricao(1).tipo("Glúten").build(),
                Restricao.builder().idRestricao(2).tipo("Lactose").build()));

        List<RestricaoResponseDTO> result = restricaoService.listarTodas();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RestricaoResponseDTO::getTipo)
                .containsExactlyInAnyOrder("Glúten", "Lactose");
    }

    @Test
    void shouldRejectSyncWithInvalidPassword() {
        SincronizacaoRequestDTO dto = SincronizacaoRequestDTO.builder().senha("wrong").build();

        assertThatThrownBy(() -> restricaoService.sincronizarComIA(dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Senha de sincronização inválida");
    }

    @Test
    void shouldSaveNewRestrictionsWhenIARespondsWithItems() {
        when(restricaoRepository.findAll()).thenReturn(List.of(Restricao.builder().tipo("Glúten").build()));
        when(geminiService.gerarListaDeRestricoes(any())).thenReturn("Lactose\nOleaginosas");

        when(restricaoRepository.saveAll(anyList())).thenReturn(List.of(
                Restricao.builder().tipo("Lactose").build(),
                Restricao.builder().tipo("Oleaginosas").build()));

        String result = restricaoService.sincronizarComIA(SincronizacaoRequestDTO.builder().senha("test-secret-key").build());

        assertThat(result).contains("2 novas restrições");
        verify(restricaoRepository).saveAll(anyList());
    }
}