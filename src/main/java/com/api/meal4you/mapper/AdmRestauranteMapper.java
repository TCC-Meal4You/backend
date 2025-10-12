package com.api.meal4you.mapper;

import com.api.meal4you.dto.AdmRestauranteRequestDTO;
import com.api.meal4you.dto.AdmRestauranteResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;

public class AdmRestauranteMapper {
    public static AdmRestaurante toEntity(AdmRestauranteRequestDTO dto) {
        return AdmRestaurante.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .build();
    }

    public static AdmRestauranteResponseDTO toResponse(AdmRestaurante admRestaurante) {
        return AdmRestauranteResponseDTO.builder()
                .nome(admRestaurante.getNome())
                .email(admRestaurante.getEmail())
                .build();
    }
}
