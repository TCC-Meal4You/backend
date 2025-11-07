package com.api.meal4you.mapper;

import com.api.meal4you.dto.UsuarioAvaliaRequestDTO;
import com.api.meal4you.dto.UsuarioAvaliaResponseDTO;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.entity.UsuarioAvalia;

public class UsuarioAvaliaMapper {
    public static UsuarioAvaliaResponseDTO toResponse(UsuarioAvalia usuarioAvalia) {
        if (usuarioAvalia == null) return null;
        return UsuarioAvaliaResponseDTO.builder()
                .idUsuario(usuarioAvalia.getUsuario().getIdUsuario())
                .idRestaurante(usuarioAvalia.getRestaurante().getIdRestaurante())
                .nota(usuarioAvalia.getNota())
                .comentario(usuarioAvalia.getComentario())
                .dataAvaliacao(usuarioAvalia.getDataAvaliacao())
                .build();
    }

    public static UsuarioAvalia toEntity(UsuarioAvaliaRequestDTO dto, Usuario usuario, Restaurante restaurante) {
        if (dto == null || usuario == null || restaurante == null) return null;
        return UsuarioAvalia.builder()
                .usuario(usuario)
                .restaurante(restaurante)
                .nota(dto.getNota())
                .comentario(dto.getComentario())
                .build();
    }
}
