package com.api.meal4you.entity;


import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UsuarioAvaliaId implements Serializable {
    private int usuario;

    private int restaurante;
}
