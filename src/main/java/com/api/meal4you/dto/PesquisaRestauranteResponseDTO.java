package com.api.meal4you.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PesquisaRestauranteResponseDTO {
    private List<RestauranteFavoritoResponseDTO> restaurantes;
    private int totalPaginas;
}
