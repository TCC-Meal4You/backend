package com.api.meal4you.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefeicaoRequestDTO {
    private String nome;

    private String descricao;

    private String tipo;

    private Boolean disponivel;

    private BigDecimal preco;

    private List<Integer> ingredientesIds;

}
