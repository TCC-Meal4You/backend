package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestauranteRequestDTO {
    private String nome;

    private String localizacao;

    private String tipo_comida;

    private boolean aberto;
    
}