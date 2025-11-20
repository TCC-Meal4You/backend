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
    
    private String logradouro;

    private int numero;

    private String complemento;

    private String bairro;

    private String cidade;

    private String uf;
    
    private String descricao;

    private String tipoComida;

    private boolean isFavorito;

    private int totalPaginas;
    
    List<RefeicaoResponseDTO> refeicoes;

}
