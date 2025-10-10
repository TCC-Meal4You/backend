package com.api.meal4you.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Restaurante;

public class RestauranteMapper {
    public static Restaurante toEntity(RestauranteRequestDTO dto, AdmRestaurante admin) {
        return Restaurante.builder()
            .nome(dto.getNome())
            .localizacao(dto.getLocalizacao())
            .tipo_comida(dto.getTipo_comida())
            .aberto(dto.isAberto())
            .admin(admin)
            .build();
    }

    public static RestauranteResponseDTO toResponse(Restaurante restaurante) {
        AdmRestaurante admin = restaurante.getAdmin();
        return RestauranteResponseDTO.builder()
            .nome(restaurante.getNome())
            .localizacao(restaurante.getLocalizacao())
            .tipo_comida(restaurante.getTipo_comida())
            .aberto(restaurante.isAberto())
            .emailAdmin(admin.getEmail())
            .nomeAdmin(admin.getNome())
            .build();
    }

        public static List<RestauranteResponseDTO> toResponseList(List<Restaurante> restaurantes) {
        return restaurantes.stream()
                .map(RestauranteMapper::toResponse)
                .collect(Collectors.toList());
    }
}
