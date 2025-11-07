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
@Table(name = "usuario_avalia")
@IdClass(UsuarioAvaliaId.class)
public class UsuarioAvalia {

    @Id
    @MapsId("usuario")
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario ;

    @Id
    @MapsId("restaurante")
    @ManyToOne
    @JoinColumn(name = "id_restaurante", nullable = false )
    private Restaurante restaurante ;

    @Column(name = "nota", nullable = false)
    private int nota;

    @NotBlank
    @Size(min = 3)
    @Column(name = "comentario", nullable = false)
    private String comentario;

    @Column(name = "data_avaliacao", nullable = false, columnDefinition = "DATE")
    private LocalDate dataAvaliacao;

}
