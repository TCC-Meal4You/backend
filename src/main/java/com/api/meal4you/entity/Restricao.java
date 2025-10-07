package com.api.meal4you.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
    private int idRestricao;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_ingrediente", referencedColumnName = "id_ingrediente", nullable = false, unique = true)
    private Ingrediente ingrediente;

    @Column(name = "com_gluten", nullable = false)
    private boolean comGluten;

    @Column(name = "com_lactose", nullable = false)
    private boolean comLactose;

    @Column(name = "com_acucar", nullable = false)
    private boolean comAcucar;

    @Column(name = "com_ovos", nullable = false)
    private boolean comOvos;

    @Column(name = "com_soja", nullable = false)
    private boolean comSoja;

    @Column(name = "com_nozes", nullable = false)
    private boolean comNozes;

    @Column(name = "com_amendoim", nullable = false)
    private boolean comAmendoim;

    @Column(name = "com_frutos_do_mar", nullable = false)
    private boolean comFrutosDoMar;

    @Column(name = "com_milho", nullable = false)
    private boolean comMilho;

    @Column(name = "com_conservantes", nullable = false)
    private boolean comConservantes;

    @Column(name = "com_corantes", nullable = false)
    private boolean comCorantes;

    @Column(name = "com_cafeina", nullable = false)
    private boolean comCafeina;

    @Column(name = "com_alcool", nullable = false)
    private boolean comAlcool;

    @Column(name = "com_oleo_de_palma", nullable = false)
    private boolean comOleoDePalma;

    @Column(name = "com_gordura_trans", nullable = false)
    private boolean comGorduraTrans;

    @Column(name = "com_adocante_artificial", nullable = false)
    private boolean comAdocanteArtificial;

    @Column(name = "com_aromatizante_artificial", nullable = false)
    private boolean comAromatizanteArtificial;

    @Column(name = "com_trigo", nullable = false)
    private boolean comTrigo;

    @Column(name = "com_ingredientes_geneticamente_modificados", nullable = false)
    private boolean comIngredientesGeneticamenteModificados;

    @Column(name = "com_sal", nullable = false)
    private boolean comSal;

    @Column(name = "com_pimenta", nullable = false)
    private boolean comPimenta;

    @Column(name = "com_fibras", nullable = false)
    private boolean comFibras;

    @Column(name = "com_fosfato", nullable = false)
    private boolean comFosfato;

    @Column(name = "com_fermento", nullable = false)
    private boolean comFermento;
}
