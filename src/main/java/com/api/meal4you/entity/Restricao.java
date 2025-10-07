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
    private int id_resticao;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_ingrediente", referencedColumnName = "id_ingrediente", nullable = false, unique = true)
    private Ingrediente ingrediente;

    @Column(name = "com_gluten", nullable = false)
    private boolean com_gluten;

    @Column(name = "com_lactose", nullable = false)
    private boolean com_lactose;

    @Column(name = "com_acucar", nullable = false)
    private boolean com_acucar;

    @Column(name = "com_ovos", nullable = false)
    private boolean com_ovos;

    @Column(name = "com_soja", nullable = false)
    private boolean com_soja;

    @Column(name = "com_nozes", nullable = false)
    private boolean com_nozes;

    @Column(name = "com_amendoim", nullable = false)
    private boolean com_amendoim;

    @Column(name = "com_frutos_do_mar", nullable = false)
    private boolean com_frutos_do_mar;

    @Column(name = "com_milho", nullable = false)
    private boolean com_milho;

    @Column(name = "com_conservantes", nullable = false)
    private boolean com_conservantes;

    @Column(name = "com_corantes", nullable = false)
    private boolean com_corantes;

    @Column(name = "com_cafeina", nullable = false)
    private boolean com_cafeina;

    @Column(name = "com_alcool", nullable = false)
    private boolean com_alcool;

    @Column(name = "com_oleo_de_palma", nullable = false)
    private boolean com_oleo_de_palma;

    @Column(name = "com_gordura_trans", nullable = false)
    private boolean com_gordura_trans;

    @Column(name = "com_adocante_artificial", nullable = false)
    private boolean com_adocante_artificial;

    @Column(name = "com_aromatizante_artificial", nullable = false)
    private boolean com_aromatizante_artificial;

    @Column(name = "com_trigo", nullable = false)
    private boolean com_trigo;

    @Column(name = "com_ingredientes_geneticamente_modificados", nullable = false)
    private boolean com_ingredientes_geneticamente_modificados;

    @Column(name = "com_sal", nullable = false)
    private boolean com_sal;

    @Column(name = "com_pimenta", nullable = false)
    private boolean com_pimenta;

    @Column(name = "com_fibras", nullable = false)
    private boolean com_fibras;

    @Column(name = "com_fosfato", nullable = false)
    private boolean com_fosfato;

    @Column(name = "com_fermento", nullable = false)
    private boolean com_fermento;
}
