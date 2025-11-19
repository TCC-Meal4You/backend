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

    private String cep;

    private String logradouro;

    private int numero;

    private String complemento;

    private String bairro;

    private String cidade;

    private String uf;

    private String descricao;

    private String tipoComida;

    private boolean ativo;

    private String emailAdmin;

    private String nomeAdmin;
}
