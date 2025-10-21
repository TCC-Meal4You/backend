package com.api.meal4you.mapper;

import java.util.List;
import java.util.stream.Collectors;

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
        List<String> restricoes = usuario.getUsuarioRestricoes().stream()
                .map(associacao -> associacao.getRestricao().getTipo())
                .collect(Collectors.toList());

        return UsuarioResponseDTO.builder()
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .restricoes(restricoes)
                .build();
    }
}
