package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestauranteFavoritoResponseDTO {
    private int idRestaurante;

    private String nome;

    private String bairro;

    private String uf;
    
    private String descricao;

    private String tipoComida;

    private boolean isFavorito;
}
