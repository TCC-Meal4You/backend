package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RestricaoRequestDTO {
    private boolean comGluten;
    private boolean comLactose;
    private boolean comAcucar;
    private boolean comOvos;
    private boolean comSoja;
    private boolean comNozes;
    private boolean comAmendoim;
    private boolean comFrutosDoMar;
    private boolean comMilho;
    private boolean comConservantes;
    private boolean comCorantes;
    private boolean comCafeina;
    private boolean comAlcool;
    private boolean comOleoDePalma;
    private boolean comGorduraTrans;
    private boolean comAdocanteArtificial;
    private boolean comAromatizanteArtificial;
    private boolean comTrigo;
    private boolean comIngredientesGeneticamenteModificados;
    private boolean comSal;
    private boolean comPimenta;
    private boolean comFibras;
    private boolean comFosfato;
    private boolean comFermento;
}