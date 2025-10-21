package com.api.meal4you.mapper;

import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Usuario;

public class LoginMapper {
     public static LoginResponseDTO toResponse(Usuario usuario, String token) {
        return LoginResponseDTO.builder()
                .id(usuario.getIdUsuario())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .token(token)
                .build();
    }

    public static LoginResponseDTO toResponse(AdmRestaurante adm, String token) {
        return LoginResponseDTO.builder()
                .id(adm.getIdAdmin())
                .nome(adm.getNome())
                .email(adm.getEmail())
                .token(token)
                .build();
    }
}
