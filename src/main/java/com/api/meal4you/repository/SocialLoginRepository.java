package com.api.meal4you.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.SocialLogin;
import com.api.meal4you.entity.Usuario;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Integer> {

    void deleteByUsuario(Usuario usuario);

    void deleteByAdm(AdmRestaurante adm);

    Optional<SocialLogin> findByProviderAndProviderId(String provider, String providerId);

}
