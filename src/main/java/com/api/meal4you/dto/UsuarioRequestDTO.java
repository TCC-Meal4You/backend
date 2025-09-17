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
public class UsuarioRequestDTO {
    @NotBlank
    private String nome;

    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String senha;

}

