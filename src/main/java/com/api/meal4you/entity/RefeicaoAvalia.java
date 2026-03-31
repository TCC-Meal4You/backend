package com.api.meal4you.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refeicao_avalia")
@IdClass(RefeicaoAvaliaId.class)
public class RefeicaoAvalia {

    @Id
    @MapsId("usuario")
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Id
    @MapsId("refeicao")
    @ManyToOne
    @JoinColumn(name = "id_refeicao", nullable = false)
    private Refeicao refeicao;

    @Column(name = "nota", nullable = false)
    private int nota;

    @NotBlank
    @Size(min = 3)
    @Column(name = "comentario", nullable = false)
    private String comentario;

    @Column(name = "data_avaliacao", nullable = false, columnDefinition = "DATE")
    private LocalDate dataAvaliacao;
}
