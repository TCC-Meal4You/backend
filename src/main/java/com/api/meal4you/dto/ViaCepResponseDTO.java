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

    public ViaCepResponseDTO(String cep, String logradouro, String bairro, String localidade, String uf) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.localidade = localidade;
        this.uf = uf;
        this.erro = false;
    }
}
