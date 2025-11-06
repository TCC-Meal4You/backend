package com.api.meal4you.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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
    @JoinColumn(name = "id_usuario", nullable = true)
    private Usuario usuario ;

    @Id
    @MapsId("restaurante")
    @ManyToOne
    @JoinColumn(name = "id_restaurante", nullable = true )
    private Restaurante restaurante ;

    @Column(name = "nota", nullable = true)
    private Integer nota;

    @Column(name = "comentario", nullable = true)
    private String comentario;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(name = "data_avaliacao", nullable = true, columnDefinition = "DATE")
    private LocalDate dataAvaliacao;

}
