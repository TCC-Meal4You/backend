package com.api.meal4you.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "restricao")
public class Restricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idRestricao;

    @NotBlank
    @Column(length = 100, unique = true, nullable = false)
    private String tipo;

    @OneToMany(mappedBy = "restricao")
    @Builder.Default
    private List<UsuarioRestricao> restricaoUsuarios = new ArrayList<>();

    @OneToMany(mappedBy = "restricao")
    @Builder.Default
    private List<IngredienteRestricao> ingredientes = new ArrayList<>();
}
