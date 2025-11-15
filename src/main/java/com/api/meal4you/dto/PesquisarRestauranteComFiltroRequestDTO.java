package com.api.meal4you.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PesquisarRestauranteComFiltroRequestDTO {
    private String nomeOuDescricao;

    private String tipoComida;
}