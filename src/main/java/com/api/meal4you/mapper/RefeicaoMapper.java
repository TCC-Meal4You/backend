package com.api.meal4you.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.api.meal4you.dto.PaginacaoRefeicoesResponseDTO;
import com.api.meal4you.dto.RefeicaoCardResponseDTO;
import com.api.meal4you.dto.RefeicaoRequestDTO;
import com.api.meal4you.dto.RefeicaoResponseDTO;
import com.api.meal4you.dto.RestricaoResponseDTO;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.RefeicaoIngrediente;
import com.api.meal4you.entity.Restaurante;

public class RefeicaoMapper {
    public static Refeicao toEntity(RefeicaoRequestDTO dto, Restaurante restaurante) {
        return Refeicao.builder()
            .nome(dto.getNome())
            .descricao(dto.getDescricao())
            .tipo(dto.getTipo())
            .disponivel(dto.getDisponivel())
            .preco(dto.getPreco())
            .restaurante(restaurante)
            .build();
    }

    public static RefeicaoResponseDTO toResponse(Refeicao refeicao) {
        List<Ingrediente> ingredientesDaRefeicao = refeicao.getRefeicaoIngredientes().stream()
            .map(RefeicaoIngrediente::getIngrediente)
            .collect(Collectors.toList());

        List<RefeicaoResponseDTO.IngredienteResumoResponseDTO> resumoIngredientes = ingredientesDaRefeicao.stream()
            .map(ingrediente -> RefeicaoResponseDTO.IngredienteResumoResponseDTO.builder()
                .idIngrediente(ingrediente.getIdIngrediente())
                .nome(ingrediente.getNome())
                .build())
            .collect(Collectors.toList());

        List<RestricaoResponseDTO> restricoesCalculadas = ingredientesDaRefeicao.stream()
            .flatMap(ingrediente -> ingrediente.getRestricoes().stream())
            .map(ingredienteRestricao -> ingredienteRestricao.getRestricao())
            .distinct()
            .map(RestricaoMapper::toResponse)
            .collect(Collectors.toList());

        return RefeicaoResponseDTO.builder()
            .idRefeicao(refeicao.getIdRefeicao())
            .nome(refeicao.getNome())
            .descricao(refeicao.getDescricao())
            .tipo(refeicao.getTipo())
            .disponivel(refeicao.getDisponivel())
            .preco(refeicao.getPreco())
            .ingrediente(resumoIngredientes)
            .restricao(restricoesCalculadas)
            .build();
    }

    public static List<RefeicaoResponseDTO> toResponseList(List<Refeicao> refeicoes) {
        return refeicoes.stream()
                .map(RefeicaoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static RefeicaoCardResponseDTO toCardResponse(Refeicao refeicao) {
    List<RestricaoResponseDTO> restricoesCalculadas = refeicao.getRefeicaoIngredientes().stream()
            .flatMap(refeicaoIngrediente -> refeicaoIngrediente.getIngrediente().getRestricoes().stream())
            .map(ingredienteRestricao -> ingredienteRestricao.getRestricao())
            .distinct()
            .map(RestricaoMapper::toResponse)
            .collect(Collectors.toList());

    return RefeicaoCardResponseDTO.builder()
            .idRefeicao(refeicao.getIdRefeicao())
            .idRestaurante(refeicao.getRestaurante().getIdRestaurante())
            .nome(refeicao.getNome())
            .descricao(refeicao.getDescricao())
            .tipo(refeicao.getTipo())
            .preco(refeicao.getPreco())
            .restricoes(restricoesCalculadas)
            .build();
}

public static PaginacaoRefeicoesResponseDTO toPaginacaoResponse(List<Refeicao> refeicoes, int totalPaginas) {
    List<RefeicaoCardResponseDTO> refeicaoCards = refeicoes.stream()
            .map(RefeicaoMapper::toCardResponse)
            .collect(Collectors.toList());

    return PaginacaoRefeicoesResponseDTO.builder()
            .refeicoes(refeicaoCards)
            .totalPaginas(totalPaginas)
            .build();
}
}
