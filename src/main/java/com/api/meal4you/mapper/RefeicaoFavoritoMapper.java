package com.api.meal4you.mapper;

import com.api.meal4you.dto.RefeicaoFavoritoResponseDTO;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.entity.RefeicaoFavorito;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RefeicaoFavoritoMapper {
    public static RefeicaoFavorito toEntity(Usuario usuario, Refeicao refeicao) {
        return RefeicaoFavorito.builder()
                .usuario(usuario)
                .refeicao(refeicao)
                .build();
    }

    public static RefeicaoFavoritoResponseDTO toResponse(Refeicao refeicao, boolean isFavorito) {
        return RefeicaoFavoritoResponseDTO.builder()
                .idRefeicao(refeicao.getIdRefeicao())
                .nome(refeicao.getNome())
                .descricao(refeicao.getDescricao())
                .tipo(refeicao.getTipo())
                .preco(refeicao.getPreco())
                .idRestaurante(refeicao.getRestaurante().getIdRestaurante())
                .nomeRestaurante(refeicao.getRestaurante().getNome())
                .isFavorito(isFavorito)
                .build();
    }

    public static List<RefeicaoFavoritoResponseDTO> toResponseList(List<Refeicao> refeicoes, Set<Integer> idsFavoritados) {
        return refeicoes.stream()
                .map(refeicao -> {
                    boolean isFav = idsFavoritados.contains(refeicao.getIdRefeicao());
                    return RefeicaoFavoritoMapper.toResponse(refeicao, isFav);
                })
                .collect(Collectors.toList());
    }
}
