package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestauranteResponseDTO {
    private int idRestaurante;

    private String nome;

    private String localizacao;

    private String descricao;

    private String tipoComida;

    private boolean ativo;

    private String emailAdmin;

    private String nomeAdmin;
}
