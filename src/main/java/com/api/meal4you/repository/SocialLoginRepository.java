package com.api.meal4you.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.meal4you.entity.SocialLogin;
import com.api.meal4you.entity.Usuario;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {

    void deleteByUsuario(Usuario usuario);

}
