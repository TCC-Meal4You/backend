package com.api.meal4you.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecomendacaoKNNResponseDTO {
    
    @JsonProperty("id_usuario")
    private Integer idUsuario;
    
    private List<Integer> recomendacoes;
    
    private String message;
}
