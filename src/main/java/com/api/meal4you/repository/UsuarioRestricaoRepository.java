package com.api.meal4you.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.meal4you.entity.Usuario;
import com.api.meal4you.entity.UsuarioRestricao;
import com.api.meal4you.entity.UsuarioRestricaoId;

public interface UsuarioRestricaoRepository extends JpaRepository<UsuarioRestricao, UsuarioRestricaoId> {
        void deleteByUsuario(Usuario usuario);
}
