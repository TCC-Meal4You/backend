package com.api.meal4you.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.api.meal4you.dto.PesquisaRestauranteResponseDTO;
import com.api.meal4you.dto.RestauranteFavoritoResponseDTO;
import com.api.meal4you.dto.RestaurantePorIdResponseDTO;
import com.api.meal4you.dto.RestauranteRequestDTO;
import com.api.meal4you.dto.RestauranteResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.RestauranteFavorito;
import com.api.meal4you.entity.Usuario;

public class RestauranteMapper {
    public static Restaurante toEntity(RestauranteRequestDTO dto, AdmRestaurante admin) {
        return Restaurante.builder()
                .nome(dto.getNome())
                .cep(dto.getCep())
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .complemento(dto.getComplemento())
                .bairro(dto.getBairro())
                .cidade(dto.getCidade())
                .uf(dto.getUf())
                .descricao(dto.getDescricao())
                .tipoComida(dto.getTipoComida())
                .ativo(dto.isAtivo())
                .admin(admin)
                .build();
    }

    public static RestauranteResponseDTO toResponse(Restaurante restaurante) {
        AdmRestaurante admin = restaurante.getAdmin();
        return RestauranteResponseDTO.builder()
                .idRestaurante(restaurante.getIdRestaurante())
                .nome(restaurante.getNome())
                .cep(restaurante.getCep())
                .logradouro(restaurante.getLogradouro())
                .numero(restaurante.getNumero())
                .complemento(restaurante.getComplemento())
                .bairro(restaurante.getBairro())
                .cidade(restaurante.getCidade())
                .uf(restaurante.getUf())
                .descricao(restaurante.getDescricao())
                .tipoComida(restaurante.getTipoComida())
                .ativo(restaurante.isAtivo())
                .emailAdmin(admin.getEmail())
                .nomeAdmin(admin.getNome())
                .build();
    }

    public static RestaurantePorIdResponseDTO toPorIdResponse(Restaurante restaurante, List<Refeicao> refeicoes,
            int totalPaginas, boolean isFavorito) {
        return RestaurantePorIdResponseDTO.builder()
                .idRestaurante(restaurante.getIdRestaurante())
                .nome(restaurante.getNome())
                .logradouro(restaurante.getLogradouro())
                .numero(restaurante.getNumero())
                .complemento(restaurante.getComplemento())
                .bairro(restaurante.getBairro())
                .cidade(restaurante.getCidade())
                .uf(restaurante.getUf())
                .descricao(restaurante.getDescricao())
                .tipoComida(restaurante.getTipoComida())
                .refeicoes(RefeicaoMapper.toResponseList(refeicoes))
                .totalPaginas(totalPaginas)
                .isFavorito(isFavorito)
                .build();
    }

    public static RestauranteFavorito toEntity(Usuario usuario, Restaurante restaurante) {
        return RestauranteFavorito.builder()
                .usuario(usuario)
                .restaurante(restaurante)
                .build();
    }

    public static RestauranteFavoritoResponseDTO toUsuarioCardResponse(Restaurante restaurante, boolean isFavorito) {
        return RestauranteFavoritoResponseDTO.builder()
                .idRestaurante(restaurante.getIdRestaurante())
                .nome(restaurante.getNome())
                .bairro(restaurante.getBairro())
                .uf(restaurante.getUf())
                .descricao(restaurante.getDescricao())
                .tipoComida(restaurante.getTipoComida())
                .isFavorito(isFavorito) 
                .build();
    }

    public static List<RestauranteFavoritoResponseDTO> toUsuarioCardResponseList(List<Restaurante> restaurantes, Set<Integer> idsFavoritados) {
        return restaurantes.stream()
                .map(restaurante -> {
                    boolean isFav = idsFavoritados.contains(restaurante.getIdRestaurante());
                    return RestauranteMapper.toUsuarioCardResponse(restaurante, isFav);
                })
                .collect(Collectors.toList());
    }

    public static PesquisaRestauranteResponseDTO toPesquisaResponse(
            List<Restaurante> restaurantesPaginados, 
            Set<Integer> idsFavoritados, 
            int totalPaginas) {
        
        List<RestauranteFavoritoResponseDTO> restaurantes = restaurantesPaginados.stream()
                .map(restaurante -> {
                    boolean isFav = idsFavoritados.contains(restaurante.getIdRestaurante());
                    return RestauranteMapper.toUsuarioCardResponse(restaurante, isFav);
                })
                .collect(Collectors.toList());
        
        return PesquisaRestauranteResponseDTO.builder()
                .restaurantes(restaurantes)
                .totalPaginas(totalPaginas)
                .build();
    }
}
