package com.api.meal4you.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginacaoRefeicoesResponseDTO {
    private List<RefeicaoCardResponseDTO> refeicoes;
    private int totalPaginas;
}