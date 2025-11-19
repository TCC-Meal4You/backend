package com.api.meal4you.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefeicaoCardResponseDTO {
    private int idRefeicao;
    
    private int idRestaurante;

    private String nome;

    private String descricao;

    private String tipo;

    private BigDecimal preco;

    private List<RestricaoResponseDTO> restricoes;
}