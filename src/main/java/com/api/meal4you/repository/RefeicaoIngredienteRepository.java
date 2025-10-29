package com.api.meal4you.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.RefeicaoIngrediente;
import com.api.meal4you.entity.RefeicaoIngredienteId;

public interface RefeicaoIngredienteRepository extends JpaRepository<RefeicaoIngrediente, RefeicaoIngredienteId> {
    boolean existsByIngrediente(Ingrediente ingrediente);

    void deleteByRefeicao(Refeicao refeicao);
}
