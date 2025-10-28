package com.api.meal4you.repository;

import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Integer> {
    
    boolean existsByNomeAndAdmin(String nome, AdmRestaurante admin);

    List<Ingrediente> findByAdmin(AdmRestaurante admRestaurante);

    Optional<Ingrediente> findByIdIngredienteAndAdmin(int id, AdmRestaurante admin);
}

