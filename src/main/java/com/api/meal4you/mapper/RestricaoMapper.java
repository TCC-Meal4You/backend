package com.api.meal4you.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.api.meal4you.dto.RestricaoResponseDTO;
import com.api.meal4you.entity.Restricao;

public class RestricaoMapper {
    public static RestricaoResponseDTO toResponse(Restricao restricao) {
        return RestricaoResponseDTO.builder()
            .idRestricao(restricao.getIdRestricao())
            .tipo(restricao.getTipo())
            .build();
    }

    public static List<RestricaoResponseDTO> toResponseList(List<Restricao> restricoes){
        return restricoes.stream()
                .map(RestricaoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
