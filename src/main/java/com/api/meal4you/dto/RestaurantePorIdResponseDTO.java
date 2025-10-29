package com.api.meal4you.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantePorIdResponseDTO {
    private int idRestaurante;

    private String nome;

    private String localizacao;

    private String descricao;

    private String tipoComida;

    private boolean aberto;

    List<RefeicaoResponseDTO> refeicoes;

    private int totalPaginas;
    
}
