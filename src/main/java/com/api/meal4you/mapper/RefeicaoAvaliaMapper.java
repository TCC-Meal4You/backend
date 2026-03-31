package com.api.meal4you.mapper;

import com.api.meal4you.dto.RefeicaoAvaliaRequestDTO;
import com.api.meal4you.dto.RefeicaoAvaliaResponseDTO;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.entity.RefeicaoAvalia;

public class RefeicaoAvaliaMapper {
    public static RefeicaoAvaliaResponseDTO toResponse(RefeicaoAvalia refeicaoAvalia) {
        if (refeicaoAvalia == null) return null;
        return RefeicaoAvaliaResponseDTO.builder()
                .idUsuario(refeicaoAvalia.getUsuario().getIdUsuario())
                .idRefeicao(refeicaoAvalia.getRefeicao().getIdRefeicao())
                .nota(refeicaoAvalia.getNota())
                .comentario(refeicaoAvalia.getComentario())
                .dataAvaliacao(refeicaoAvalia.getDataAvaliacao())
                .build();
    }

    public static RefeicaoAvalia toEntity(RefeicaoAvaliaRequestDTO dto, Usuario usuario, Refeicao refeicao) {
        if (dto == null || usuario == null || refeicao == null) return null;
        return RefeicaoAvalia.builder()
                .usuario(usuario)
                .refeicao(refeicao)
                .nota(dto.getNota())
                .comentario(dto.getComentario())
                .build();
    }
}
