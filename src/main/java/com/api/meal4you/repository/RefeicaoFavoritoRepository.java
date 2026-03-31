package com.api.meal4you.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.RefeicaoFavorito;
import com.api.meal4you.entity.RefeicaoFavoritoId;

import com.api.meal4you.entity.Usuario;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefeicaoFavoritoRepository extends JpaRepository<RefeicaoFavorito, RefeicaoFavoritoId> {

    List<RefeicaoFavorito> findByUsuario(Usuario usuario);

    Optional<RefeicaoFavorito> findByUsuarioAndRefeicao(Usuario usuario, Refeicao refeicao);

    void deleteByUsuario(Usuario usuario);

    void deleteByRefeicao(Refeicao refeicao);
}
