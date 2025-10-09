package com.api.meal4you.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredienteResponseDTO {
    private int idIngrediente;

    private String nome;
    
    private int IdAdmin;

    private List<RestricaoResponseDTO> restricoes;
}
