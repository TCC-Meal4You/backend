package com.api.meal4you.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"nome", "localizacao"})
)

public class Restaurante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_restaurante;

    @NotNull
    @NotBlank
    @Size(min = 3,max = 120)
    @Column(length = 120)
    private String nome;

    @NotNull
    @NotBlank
    @Size(min = 4,max = 200)
    @Column(length = 200)
    private String localizacao;

    @NotNull
    @NotBlank
    @Size(min = 3,max = 100)
    @Column(length = 100)
    private String tipo_comida;

    private boolean aberto;

    @NotNull
    @OneToOne
    @JoinColumn(name = "id_admin", referencedColumnName = "id_admin", nullable = false)
    private AdmRestaurante admin;
}
