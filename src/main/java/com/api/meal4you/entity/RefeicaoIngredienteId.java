package com.api.meal4you.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RefeicaoIngredienteId implements Serializable {
    private int refeicao;

    private int ingrediente;
}
