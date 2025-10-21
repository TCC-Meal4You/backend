package com.api.meal4you.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRestricaoRequestDTO {
    
    private List<Integer> restricaoIds;

}
