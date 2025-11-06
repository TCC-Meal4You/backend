package com.api.meal4you.repository;

import com.api.meal4you.entity.*;
import org.hibernate.sql.Delete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioAvaliaRepository extends JpaRepository<UsuarioAvalia, UsuarioAvaliaId> {

    Optional<UsuarioAvalia> findByUsuarioAndRestaurante(Usuario usuario, Restaurante idRestaurante);

    List<UsuarioAvalia> findByUsuario(Usuario usuario);

    void deleteByUsuario(Usuario usuario);

    void deleteByRestaurante(Restaurante restaurante);

    List<UsuarioAvalia> findByRestaurante(Restaurante restaurante);

}
