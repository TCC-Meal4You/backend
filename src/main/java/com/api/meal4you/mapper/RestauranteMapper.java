package com.api.meal4you.mapper;

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
        return RestauranteResponseDTO.builder()
            .nome(restaurante.getNome())
            .localizacao(restaurante.getLocalizacao())
            .tipo_comida(restaurante.getTipo_comida())
            .aberto(restaurante.isAberto())
            .emailAdmin(restaurante.getAdmin().getEmail())
            .nomeAdmin(restaurante.getAdmin().getNome())
            .build();
    }
}
