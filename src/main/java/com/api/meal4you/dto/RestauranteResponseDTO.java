package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestauranteResponseDTO {
    private String nome;

    private String localizacao;

    private String tipoComida;

    private boolean aberto;

    private String emailAdmin;

    private String nomeAdmin;
}
