package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredienteResponseDTO {
    private String nome;
    
    private String emailAdmin;

    private String nomeAdmin;

    private RestricaoResponseDTO restricao;
}
