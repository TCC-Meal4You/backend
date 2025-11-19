package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViaCepResponseDTO {
    private String cep;

    private String logradouro;
    
    private String bairro;
    
    private String localidade;
    
    private String uf;
    
    private boolean erro;
}
