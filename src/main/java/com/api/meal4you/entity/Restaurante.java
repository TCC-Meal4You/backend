package com.api.meal4you.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
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
@Table(
    name="restaurante",
    uniqueConstraints = @UniqueConstraint(columnNames ={"nome", "localizacao"})
)
public class Restaurante {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idRestaurante;

    @NotBlank
    @Size(min = 3, max = 120)
    @Column(length = 120)
    private String nome;

    @NotBlank
    @Size(min = 4, max = 200)
    @Column(length = 200)
    private String localizacao;

    @NotBlank
    @Size(min = 3, max = 100)
    @Column(length = 100)
    private String tipoComida;

    private boolean aberto;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_admin", referencedColumnName = "idAdmin", nullable = false)
    private AdmRestaurante admin;

}
