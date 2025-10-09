package com.api.meal4you.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestricaoResponseDTO {
    private int idRestricao;

    private String tipo;

}
