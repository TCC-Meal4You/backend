package com.api.meal4you.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefeicaoAvaliaRequestDTO {

    private int idRefeicao;

    private int nota;

    private String comentario;
}
