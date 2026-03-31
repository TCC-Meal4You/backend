package com.api.meal4you.dto;

import lombok.*;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefeicaoAvaliaResponseDTO {

    private int idUsuario;

    private int idRefeicao;

    private int nota;

    private String comentario;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataAvaliacao;
}
