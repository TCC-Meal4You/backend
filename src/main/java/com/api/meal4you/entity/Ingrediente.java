package com.api.meal4you.entity;

import java.util.List;   

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "ingrediente")
public class Ingrediente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idIngrediente;

    @NotBlank
    @Size(min = 3, max = 150)
    @Column(length = 150, unique = true)
    private String nome;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_admin", referencedColumnName = "idAdmin", nullable = false)
    private AdmRestaurante admin;

    @OneToMany(mappedBy = "ingrediente")
    private List<RefeicaoIngrediente> refeicaoIngredientes;

    @OneToMany(mappedBy = "ingrediente")
    private List<IngredienteRestricao> restricoes;
}
