package com.api.meal4you.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefeicaoFavoritoResponseDTO {
    private int idRefeicao;

    private String nome;

    private String descricao;

    private String tipo;

    private BigDecimal preco;

    private int idRestaurante;

    private String nomeRestaurante;

    private boolean isFavorito;
}
