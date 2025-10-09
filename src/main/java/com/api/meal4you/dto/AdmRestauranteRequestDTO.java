package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmRestauranteRequestDTO {
    private String nome;

    private String email;

    private String senha;
}
