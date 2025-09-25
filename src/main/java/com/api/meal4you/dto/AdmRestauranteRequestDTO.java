package com.api.meal4you.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AdmRestauranteRequestDTO {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 3, max = 150)
    private String nome;
    @NotBlank
    @Size(min = 6, max = 60)
    private String senha;
}
