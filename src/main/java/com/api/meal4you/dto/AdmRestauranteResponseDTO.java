package com.api.meal4you.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmRestauranteResponseDTO {
    private String email;
    private String nome;
}
