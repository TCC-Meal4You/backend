package com.api.meal4you.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.meal4you.dto.PaginacaoRefeicoesResponseDTO;
import com.api.meal4you.dto.PesquisarRefeicaoComFiltroRequestDTO;
import com.api.meal4you.dto.RefeicaoRequestDTO;
import com.api.meal4you.dto.RefeicaoResponseDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.RefeicaoIngrediente;
import com.api.meal4you.entity.Restaurante;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.entity.UsuarioRestricao;
import com.api.meal4you.mapper.RefeicaoMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefeicaoService {

    private final UsuarioService usuarioService;
    private final RefeicaoRepository refeicaoRepository;
    private final AdmRestauranteService admRestauranteService;
    private final AdmRestauranteRepository admRestauranteRepository;
    private final RestauranteRepository restauranteRepository;
    private final IngredienteRepository ingredienteRepository;
    private final RefeicaoIngredienteRepository refeicaoIngredienteRepository;
    private final UsuarioRepository usuarioRepository;

    
    @Transactional
    public RefeicaoResponseDTO cadastrarRefeicao(RefeicaoRequestDTO dto) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));
            
            Restaurante restaurante = restauranteRepository.findByAdmin(adminExistente)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado. Você precisa cadastrar um restaurante primeiro."));

            if (dto.getIngredientesIds() == null || dto.getIngredientesIds().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A refeição deve ter pelo menos 1 ingrediente.");
            }

            if (refeicaoRepository.existsByNomeAndRestaurante(dto.getNome(), restaurante)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já possui uma refeição com este nome no seu cardápio.");
            }

            List<Ingrediente> ingredientes = ingredienteRepository.findAllById(dto.getIngredientesIds());

            if (ingredientes.size() != dto.getIngredientesIds().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais IDs de ingredientes são inválidos.");
            }

            for (Ingrediente ing : ingredientes) {
                if (ing.getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode adicionar ingredientes que não pertencem a você.");
                }
            }
            
            Refeicao novaRefeicao = RefeicaoMapper.toEntity(dto, restaurante);
            refeicaoRepository.save(novaRefeicao);

            List<RefeicaoIngrediente> associacoes = ingredientes.stream()
                .map(ingrediente -> RefeicaoIngrediente.builder()
                    .refeicao(novaRefeicao)
                    .ingrediente(ingrediente)
                    .build())
                .collect(Collectors.toList());
            
            refeicaoIngredienteRepository.saveAll(associacoes);
            novaRefeicao.setRefeicaoIngredientes(associacoes);

            return RefeicaoMapper.toResponse(novaRefeicao);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Erro ao cadastrar refeição: " + ex.getMessage());
        }
    }

    @Transactional
    public PaginacaoRefeicoesResponseDTO listarTodas(Integer numPagina) {
        try {
            String emailUsuarioLogado = usuarioService.getUsuarioLogadoEmail();
            if (emailUsuarioLogado == null || emailUsuarioLogado.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
            }
            
            // Busca todas as refeições disponíveis de uma vez
            List<Refeicao> refeicoesDisponiveis = refeicaoRepository.findAllByDisponivelTrue();
        
            // Lógica de paginação
            int tamanhoPagina = 10;
            int pagina = (numPagina != null && numPagina > 0) ? numPagina : 1;
            int inicio = (pagina - 1) * tamanhoPagina;
            
            if (inicio >= refeicoesDisponiveis.size()) {
                // Retorna uma lista vazia se o número da página for inválido
                return new PaginacaoRefeicoesResponseDTO(Collections.emptyList(), 0);
            }
        
            int fim = Math.min(inicio + tamanhoPagina, refeicoesDisponiveis.size());
        
            int totalRefeicoesDisponiveis = refeicoesDisponiveis.size();
            int totalPaginas = (int) Math.ceil((double) totalRefeicoesDisponiveis / tamanhoPagina);
        
            List<Refeicao> refeicoesPaginadas = refeicoesDisponiveis.subList(inicio, fim);
        
            // Usa método do mapper para converter e retornar a resposta paginada
            return RefeicaoMapper.toPaginacaoResponse(refeicoesPaginadas, totalPaginas);
        
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Erro ao listar refeições: " + ex.getMessage());
        }
    }

    // Esse método é auxiliar de pesquisarComFiltro e calcula a compatibilidade de uma refeição com as restrições do usuário. Retorna o número de restrições do usuário que NÃO são violadas pela refeição. Quanto maior o valor, mais compatível a refeição é com o usuário.
    private int calcularCompatibilidade(Refeicao refeicao, Set<Integer> idsRestricoesUsuario) {
        if (idsRestricoesUsuario.isEmpty()) {
            return 0; // Se o usuário não tem restrições, todas têm compatibilidade 0 (neutra)
        }
        
        // Coletar todas as restrições violadas pelos ingredientes da refeição
        Set<Integer> restricoesVioladas = refeicao.getRefeicaoIngredientes().stream()
                .flatMap(ri -> ri.getIngrediente().getRestricoes().stream())
                .map(ir -> ir.getRestricao().getIdRestricao())
                .filter(idsRestricoesUsuario::contains) // Apenas restrições que o usuário possui
                .collect(Collectors.toSet());
        
        // Retorna quantas restrições do usuário NÃO são violadas
        return idsRestricoesUsuario.size() - restricoesVioladas.size();
    }

    @Transactional
    public PaginacaoRefeicoesResponseDTO pesquisarComFiltro(PesquisarRefeicaoComFiltroRequestDTO dto, Integer numPagina) {
        try {
            String emailUsuarioLogado = usuarioService.getUsuarioLogadoEmail();
            if (emailUsuarioLogado == null || emailUsuarioLogado.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
            }
            
            // Busca todas as refeições disponíveis
            List<Refeicao> todasRefeicoes = refeicaoRepository.findAllByDisponivelTrue();
            
            // Busca as restrições do usuário logado
            Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));
            
            List<UsuarioRestricao> usuarioRestricoes = usuario.getUsuarioRestricoes();
            Set<Integer> idsRestricoesUsuario = usuarioRestricoes.stream()
                    .map(ur -> ur.getRestricao().getIdRestricao())
                    .collect(Collectors.toSet());
            
            // Preparar filtros
            final String termoBusca = dto.getNomeOuDescricao() != null ? dto.getNomeOuDescricao().toLowerCase() : null;
            final String tipoFiltro = dto.getTipo() != null ? dto.getTipo().toLowerCase() : null;
            final BigDecimal precoMaximo = dto.getPrecoMaximo();
            
            // Filtrar refeições com base nos critérios fornecidos
            List<Refeicao> refeicoesFiltradas = todasRefeicoes.stream()
                    .filter(r -> {
                        // Filtro por nome OU descrição
                        boolean matchNomeOuDescricao = (termoBusca == null || termoBusca.isBlank()) ||
                                (r.getNome().toLowerCase().contains(termoBusca) ||
                                 r.getDescricao().toLowerCase().contains(termoBusca));
                        
                        // Filtro por tipo
                        boolean matchTipo = (tipoFiltro == null || tipoFiltro.isBlank()) ||
                                r.getTipo().toLowerCase().contains(tipoFiltro);
                        
                        // Filtro por preço (menor ou igual ao preço máximo)
                        boolean matchPreco = (precoMaximo == null) ||
                                r.getPreco().compareTo(precoMaximo) <= 0;
                        
                        return matchNomeOuDescricao && matchTipo && matchPreco;
                    })
                    .collect(Collectors.toList());
            
            // Calcular compatibilidade de cada refeição com as restrições do usuário
            // e ordenar por compatibilidade (maior compatibilidade primeiro)
            List<Refeicao> refeicoesOrdenadas = refeicoesFiltradas.stream()
                    .sorted((r1, r2) -> {
                        int compatibilidade1 = calcularCompatibilidade(r1, idsRestricoesUsuario);
                        int compatibilidade2 = calcularCompatibilidade(r2, idsRestricoesUsuario);
                        return Integer.compare(compatibilidade2, compatibilidade1); // Ordem decrescente
                    })
                    .collect(Collectors.toList());
            
            // Paginação
            int tamanhoPagina = 10;
            int pagina = (numPagina != null && numPagina > 0) ? numPagina : 1;
            int inicio = (pagina - 1) * tamanhoPagina;
            
            if (inicio >= refeicoesOrdenadas.size()) {
                return new PaginacaoRefeicoesResponseDTO(Collections.emptyList(), 0);
            }
            
            int fim = Math.min(inicio + tamanhoPagina, refeicoesOrdenadas.size());
            int totalPaginas = (int) Math.ceil((double) refeicoesOrdenadas.size() / tamanhoPagina);
            
            if (totalPaginas == 0 && !refeicoesOrdenadas.isEmpty()) {
                totalPaginas = 1;
            }
            
            List<Refeicao> refeicoesPaginadas = refeicoesOrdenadas.subList(inicio, fim);
            
            return RefeicaoMapper.toPaginacaoResponse(refeicoesPaginadas, totalPaginas);
            
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao filtrar e listar refeições: " + ex.getMessage());
        }
    }

    @Transactional
    public List<RefeicaoResponseDTO> listarMinhasRefeicoes() {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            Restaurante restaurante = restauranteRepository.findByAdmin(adminExistente)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado para este administrador."));
            
            List<Refeicao> refeicoes = refeicaoRepository.findByRestaurante(restaurante);

            return RefeicaoMapper.toResponseList(refeicoes);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Erro ao listar refeições: " + ex.getMessage());
        }
    }

    @Transactional
    public RefeicaoResponseDTO atualizarRefeicao(int id, RefeicaoRequestDTO dto) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            Refeicao refeicao = refeicaoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refeição não encontrada."));

            if (refeicao.getRestaurante().getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para alterar esta refeição.");
            }

            boolean alterado = false;

            if (dto.getNome() != null && !dto.getNome().isBlank() && !dto.getNome().equals(refeicao.getNome())) {
                if (refeicaoRepository.existsByNomeAndRestauranteAndIdRefeicaoNot(dto.getNome(), refeicao.getRestaurante(), id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já possui outra refeição com este nome no seu cardápio.");
                }
                refeicao.setNome(dto.getNome());
                alterado = true;
            }
            if (dto.getDescricao() != null && !dto.getDescricao().equals(refeicao.getDescricao())) {
                refeicao.setDescricao(dto.getDescricao());
                alterado = true;
            }
            if (dto.getTipo() != null && !dto.getTipo().isBlank() && !dto.getTipo().equals(refeicao.getTipo())) {
                refeicao.setTipo(dto.getTipo());
                alterado = true;
            }
            if (dto.getPreco() != null && dto.getPreco().compareTo(refeicao.getPreco()) != 0) {
                refeicao.setPreco(dto.getPreco());
                alterado = true;
            }

            if (dto.getIngredientesIds() != null) {
                if (dto.getIngredientesIds().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A refeição deve ter pelo menos 1 ingrediente.");
                }

                refeicaoIngredienteRepository.deleteByRefeicao(refeicao);

                List<Ingrediente> novosIngredientes = ingredienteRepository.findAllById(dto.getIngredientesIds());
                if (novosIngredientes.size() != dto.getIngredientesIds().size()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais IDs de ingredientes são inválidos.");
                }
                for (Ingrediente ing : novosIngredientes) {
                    if (ing.getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode adicionar ingredientes que não pertencem a você.");
                    }
                }
                
                List<RefeicaoIngrediente> novasAssociacoes = novosIngredientes.stream()
                    .map(ingrediente -> RefeicaoIngrediente.builder().refeicao(refeicao).ingrediente(ingrediente).build())
                    .collect(Collectors.toList());
                refeicaoIngredienteRepository.saveAll(novasAssociacoes);
                refeicao.setRefeicaoIngredientes(novasAssociacoes);
                
                alterado = true;
            }

            if (!alterado) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
            }

            refeicaoRepository.save(refeicao);
            return RefeicaoMapper.toResponse(refeicao);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao atualizar refeição: " + ex.getMessage());
        }
    }

    @Transactional
    public void deletarRefeicao(int id) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            Refeicao refeicao = refeicaoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refeição não encontrada."));

            if (refeicao.getRestaurante().getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para deletar esta refeição.");
            }

            refeicaoIngredienteRepository.deleteByRefeicao(refeicao);

            refeicaoRepository.delete(refeicao);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Erro ao deletar refeição: " + ex.getMessage());
        }
    }

    @Transactional
    public RefeicaoResponseDTO atualizarDisponibilidade(int id, boolean disponivel) {
        try {
            String emailAdmLogado = admRestauranteService.getAdmLogadoEmail();
            AdmRestaurante adminExistente = admRestauranteRepository.findByEmail(emailAdmLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));
            
            Refeicao refeicao = refeicaoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refeição não encontrada."));

            if (refeicao.getRestaurante().getAdmin().getIdAdmin() != adminExistente.getIdAdmin()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para alterar esta refeição.");
            }

            if (refeicao.getDisponivel() == disponivel) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada. A refeição já está neste estado.");
            }

            refeicao.setDisponivel(disponivel);
            refeicaoRepository.save(refeicao);

            return RefeicaoMapper.toResponse(refeicao);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Erro ao atualizar disponibilidade: " + ex.getMessage());
        }
    }
}
