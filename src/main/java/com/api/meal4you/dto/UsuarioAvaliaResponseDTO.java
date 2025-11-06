package com.api.meal4you.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAvaliaResponseDTO {

    private int idUsuario;


    private int idRestaurante;


    private Integer nota;


    private String comentario;


    private LocalDate dataAvaliacao;
}