package com.api.meal4you.repository;

import com.api.meal4you.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefeicaoAvaliaRepository extends JpaRepository<RefeicaoAvalia, RefeicaoAvaliaId> {

    Optional<RefeicaoAvalia> findByUsuarioAndRefeicao(Usuario usuario, Refeicao refeicao);

    List<RefeicaoAvalia> findByUsuario(Usuario usuario);

    void deleteByUsuario(Usuario usuario);

    void deleteByRefeicao(Refeicao refeicao);

    List<RefeicaoAvalia> findByRefeicao(Refeicao refeicao);

    boolean existsByUsuarioAndRefeicao(Usuario usuario, Refeicao refeicao);
}
