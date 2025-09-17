package com.api.meal4you.mapper;

import com.api.meal4you.dto.UsuarioRequestDTO;
import com.api.meal4you.dto.UsuarioResponseDTO;
import com.api.meal4you.entity.Usuario;

    public class UsuarioMapper {

        public static Usuario toEntity(UsuarioRequestDTO dto) {
            return Usuario.builder()
                    .nome(dto.getNome())
                    .email(dto.getEmail())
                    .senha(dto.getSenha())
                    .build();
        }

        public static UsuarioResponseDTO toResponse(Usuario usuario) {
            return UsuarioResponseDTO.builder()
                    .nome(usuario.getNome())
                    .email(usuario.getEmail())
                    .build();
        }
    }

