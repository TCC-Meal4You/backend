package com.api.meal4you.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefeicaoResponseDTO {
    private int idRefeicao;

    private String descricao;

    private BigDecimal preco;

    private String tipo;

    private String disponivel;

    private List<IngredienteResumoResponseDTO> ingrediente;

    private List<RestricaoResponseDTO> restricao;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngredienteResumoResponseDTO {
        private int idIngrediente;

        private String nome;
    }
}
