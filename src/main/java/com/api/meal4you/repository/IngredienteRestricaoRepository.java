package com.api.meal4you.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.IngredienteRestricao;
import com.api.meal4you.entity.IngredienteRestricaoId;

public interface IngredienteRestricaoRepository extends JpaRepository<IngredienteRestricao, IngredienteRestricaoId> {
    void deleteByIngrediente(Ingrediente ingrediente);
}
