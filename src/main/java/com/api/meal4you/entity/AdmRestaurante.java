package com.api.meal4you.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class AdmRestaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @NotBlank
    @Size(max = 200)
    @Column(length = 200,unique = true)
    private String email;

    @NotNull
    @NotBlank
    @Size(min = 3,max = 150)
    @Column(length = 150)
    private String nome;

    @NotNull
    @NotBlank
    @Size(min = 6,max = 60)
    @Column(length = 60)
    private String senha;
}
