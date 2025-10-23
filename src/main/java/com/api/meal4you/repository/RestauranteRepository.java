package com.api.meal4you.repository;

import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Restaurante;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RestauranteRepository extends JpaRepository<Restaurante, Integer> {
    Optional<Restaurante> findByNomeAndLocalizacao(String nome, String localizacao);

    Optional<Restaurante> findByAdmin(AdmRestaurante admin);
}