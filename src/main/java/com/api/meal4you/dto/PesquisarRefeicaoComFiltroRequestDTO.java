package com.api.meal4you.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PesquisarRefeicaoComFiltroRequestDTO {
    private String nomeOuDescricao;

    private String tipo;

    private BigDecimal precoMaximo;
}
