package com.api.meal4you.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.RestauranteFavorito;
import com.api.meal4you.entity.RestauranteFavoritoId;

import com.api.meal4you.entity.Usuario;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestauranteFavoritoRepository extends JpaRepository<RestauranteFavorito, RestauranteFavoritoId> {

    List<RestauranteFavorito> findByUsuario(Usuario usuario);

    Optional<RestauranteFavorito> findByUsuarioAndRestaurante(Usuario usuario, Restaurante restaurante);

    void deleteByUsuario(Usuario usuario);

    void deleteByRestaurante(Restaurante restaurante);
}
