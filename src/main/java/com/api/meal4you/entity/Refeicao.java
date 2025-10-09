package com.api.meal4you.entity;

import java.math.BigDecimal;
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
@Table(name = "refeicao")
public class Refeicao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idRefeicao;

    @NotBlank
    @Size(min = 3, max = 120)
    @Column(length = 120)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Size(min = 6, max = 100)
    @Column(length = 100)
    private String tipo;

    @NotNull
    private Boolean disponivel;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal preco;

    @ManyToOne
    @JoinColumn(name = "id_restaurante", nullable = false)
    private Restaurante restaurante;

    @OneToMany(mappedBy = "refeicao")
    private List<RefeicaoIngrediente> refeicaoIngredientes;
}
