package com.api.meal4you.dto;

import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.Restricao;
import com.api.meal4you.entity.Usuario;
import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAvaliaRequestDTO {

    private int idUsuario;


    private int idRestaurante;


    private Integer nota;


    private String comentario;


    private LocalDate dataAvaliacao;
}
