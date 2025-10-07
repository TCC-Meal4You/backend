package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredienteRequestDTO {
    private String nome;
    
    private RestricaoDTO restricao;
}
