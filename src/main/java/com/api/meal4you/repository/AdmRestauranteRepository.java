package com.api.meal4you.repository;

import com.api.meal4you.entity.AdmRestaurante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdmRestauranteRepository extends JpaRepository<AdmRestaurante, Integer> {
    Optional<AdmRestaurante> findByEmail(String email);

    void deleteByEmail(String email);
}
