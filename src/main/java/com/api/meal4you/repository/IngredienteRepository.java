package com.api.meal4you.repository;

import com.api.meal4you.entity.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Integer> {
    boolean existsByNome(String nome);
}

