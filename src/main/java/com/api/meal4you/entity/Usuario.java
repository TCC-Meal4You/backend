package com.api.meal4you.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuario")

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_usuario;

    @NotBlank
    @Size(min = 3,max = 150)
    @Column(length = 150)
    private String nome;
    
    @NotBlank
    @Size(max = 200)
    @Column(length = 200,unique = true)
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 6,max = 60)
    @Column(length = 60)
    private String senha;
}
