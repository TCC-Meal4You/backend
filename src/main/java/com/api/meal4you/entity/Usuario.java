package com.api.meal4you.entity;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.*;
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
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Size(max = 150)
    @Column(length = 150)
    private String nome;
    
    @NotNull
    @Size(max = 200)
    @Column(length = 200,unique = true)
    private String email;
    
    @NotNull
    @Size(max = 60)
    @Column(length = 60)
    private String senha;
    
    @NotNull
    @Size(max = 200)
    @Column(length = 200)
    private String localizacao;
    
    @NotNull
    @Schema(type = "string", example = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date data_nascimento;
    
    private Integer tempo_disponivel;
    //private Preferencias id_preferencia; Colocar quando tiver a entidade preferencias
}
