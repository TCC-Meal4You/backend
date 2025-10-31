package com.api.meal4you.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.Restaurante;

public interface RefeicaoRepository extends JpaRepository<Refeicao, Integer> {
    List<Refeicao> findByRestaurante(Restaurante restaurante);

    boolean existsByNomeAndRestaurante(String nome, Restaurante restaurante);

    boolean existsByNomeAndRestauranteAndIdRefeicaoNot(String nome, Restaurante restaurante, int idRefeicao);
}
