package com.api.meal4you.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.api.meal4you.dto.IngredienteRequestDTO;
import com.api.meal4you.dto.IngredienteResponseDTO;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.IngredienteRestricao;

public class IngredienteMapper {

    public static Ingrediente toEntity(IngredienteRequestDTO dto) {
        return Ingrediente.builder()
                .nome(dto.getNome())
                .build();
    }

    public static IngredienteResponseDTO toResponse(Ingrediente ingrediente) {
        return IngredienteResponseDTO.builder()
                .idIngrediente(ingrediente.getIdIngrediente())
                .nome(ingrediente.getNome())
                .IdAdmin(ingrediente.getAdmin().getIdAdmin())
                .restricoes(ingrediente.getRestricoes().stream()
                        .map(IngredienteRestricao::getRestricao)
                        .map(RestricaoMapper::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public static List<IngredienteResponseDTO> toResponseList(List<Ingrediente> ingredientes) {
        return ingredientes.stream()
                .map(IngredienteMapper::toResponse)
                .collect(Collectors.toList());
    }
}