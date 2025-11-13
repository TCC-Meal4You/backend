package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedefinirSenhaRequestDTO {
    private String email;
    
    private String codigoVerificacao;
    
    private String novaSenha;
}
