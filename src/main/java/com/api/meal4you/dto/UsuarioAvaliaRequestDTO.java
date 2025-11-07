package com.api.meal4you.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAvaliaRequestDTO {

    private int idRestaurante;


    private Integer nota;


    private String comentario;

}
