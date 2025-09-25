package com.api.meal4you.entity;

import jakarta.persistence.*;   
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity

public class Ingrediente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_ingrediente;

    @NotBlank
    @Size(min = 3, max = 150)
    @Column(length = 150, unique=true)
    private String descricao;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_admin", referencedColumnName = "id_admin", nullable = false)
    private AdmRestaurante admin;

    @ManyToMany(mappedBy = "ingredientes")
    private List<Refeicao> refeicoes;
}
