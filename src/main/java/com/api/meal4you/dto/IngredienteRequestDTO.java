package com.api.meal4you.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredienteRequestDTO {
    private String nome;
    
    private List<Integer> restricoesIds;
}
