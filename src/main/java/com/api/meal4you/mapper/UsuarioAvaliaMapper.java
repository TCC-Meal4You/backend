package com.api.meal4you.mapper;

import com.api.meal4you.dto.UsuarioAvaliaRequestDTO;
import com.api.meal4you.dto.UsuarioAvaliaResponseDTO;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.entity.UsuarioAvalia;

public class UsuarioAvaliaMapper {
    public static UsuarioAvaliaResponseDTO toResponse(UsuarioAvalia entity) {
        if (entity == null) return null;
        return UsuarioAvaliaResponseDTO.builder()
                .idUsuario(entity.getUsuario().getIdUsuario())
                .idRestaurante(entity.getRestaurante().getIdRestaurante())
                .nota(entity.getNota())
                .comentario(entity.getComentario())
                .dataAvaliacao(entity.getDataAvaliacao())
                .build();
    }

    public static UsuarioAvalia toEntity(UsuarioAvaliaRequestDTO dto, Usuario usuario, Restaurante restaurante) {
        if (dto == null || usuario == null || restaurante == null) return null;
        return UsuarioAvalia.builder()
                .usuario(usuario)
                .restaurante(restaurante)
                .nota(dto.getNota())
                .comentario(dto.getComentario())
                .dataAvaliacao(dto.getDataAvaliacao())
                .build();
    }
}
