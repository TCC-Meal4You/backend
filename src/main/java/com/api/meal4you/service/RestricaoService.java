package com.api.meal4you.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.meal4you.dto.RestricaoResponseDTO;
import com.api.meal4you.dto.SincronizacaoRequestDTO;
import com.api.meal4you.entity.Restricao;
import com.api.meal4you.mapper.RestricaoMapper;
import com.api.meal4you.repository.RestricaoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestricaoService {

    private final RestricaoRepository restricaoRepository;
    private final GeminiService geminiService;

    @Value("${senha.secreta.sincronizacao}")
    private String senhaSecreta;

    @Transactional
    public List<RestricaoResponseDTO> listarTodas() {
        try {
            List<Restricao> restricoes = restricaoRepository.findAll();
            return RestricaoMapper.toResponseList(restricoes);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao listar restrições: " + ex.getMessage());
        }
    }

    @Transactional
    public RestricaoResponseDTO buscarPorId(int id) {
        try {
            Restricao restricao = restricaoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Restrição não encontrada para o ID: " + id));
            return RestricaoMapper.toResponse(restricao);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao encontrar restrição: " + ex.getMessage());
        }
    }

    // Para o Controller (com senha)
    public String sincronizarComIA(SincronizacaoRequestDTO dto) {
        try {
            if (dto.getSenha() == null || !dto.getSenha().equals(senhaSecreta)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha de sincronização inválida!");
            }
            return executarLogicaDeSincronizacao();
        } catch (ResponseStatusException ex) {
            throw ex;
        }
    }

    // Para o Robô Scheduler(sem senha)
    public String sincronizarComIA() {
        return executarLogicaDeSincronizacao();
    }

    @Transactional
    private String executarLogicaDeSincronizacao() {

        Set<String> tiposExistentes = restricaoRepository.findAll()
                .stream()
                .map(Restricao::getTipo)
                .collect(Collectors.toSet());

        String listaParaPrompt = tiposExistentes.isEmpty() ? "Nenhuma" : String.join(", ", tiposExistentes);

        //Melhorar o prompt
       String prompt = String.format(
        "Você é um **arquiteto de dados** criando a **tabela mestre** de grupos alergênicos para um sistema de food service. O objetivo é ter uma lista limpa, de alto nível e sem redundância, que será usada por restaurantes e consumidores." +

        "\n\n**CONTEXTO E OBJETIVO DO PROJETO:**" +
        "\nEstou criando a lista principal de restrições alimentares do aplicativo. " +
        "Donos de restaurante usarão esta lista para marcar os ingredientes de seus pratos (ex: 'este prato contém Glúten'). " +
        "Depois, os usuários finais usarão a MESMA lista para selecionar suas próprias restrições (ex: 'eu sou intolerante a Glúten') e encontrar pratos compatíveis. " +
        "Portanto, a lista precisa ser **clara, de alto nível e facilmente reconhecível** tanto por leigos quanto por profissionais da alimentação." +

        "\n\n**REGRA DE OURO (A MAIS IMPORTANTE):**" +
        "\nSua principal tarefa é a **GENERALIZAÇÃO**. Você DEVE retornar o **grupo ou componente** mais genérico que causa a restrição, e NUNCA o alimento específico que o contém." +

        "\n\n**EXEMPLOS PRÁTICOS:**" +
        "\n- Se um item já pertence a um grupo maior, retorne o grupo. Ex: 'Lactose' (e não 'Leite'), 'Glúten' (e não 'Trigo'), 'Oleaginosas' (e não 'Amendoim')." +
        "\n- Se a lista existente já contém algum grupo não adicione alimentos pertencentes a esse grupo. Ex: 'Oleaginosas', **NÃO** adicione 'Amendoim', pois ele já pertence a esse grupo." +

        "\n\n**REGRAS DE SAÍDA E CONTROLE:**" +
        "\n1. **USABILIDADE:** Nao use termos muito técnicos ou raros que um usuário comum não reconheceria (ex: 'Proteínas do Tremoço')." +
        "\n2. **FORMATAÇÃO:** Retorne cada novo grupo em uma nova linha, sem marcadores ou números." +
        "\n3. **SEM REPETIÇÃO:** Não repita itens da lista fornecida abaixo." +
        "\n4. **CASO VAZIO:** Se a lista existente já for completa, retorne a palavra 'VAZIO'." +

        "\n\n**TAREFA:**" +
        "\nCom base em todas as regras acima, gere novos grupos de restrição que ainda não estão na lista abaixo." +

        "\n\nA lista de grupos que já existem no meu sistema é: [%s]" +
        "\n\nNovos grupos alergênicos:",
        listaParaPrompt);

        String respostaIA = geminiService.gerarListaDeRestricoes(prompt);

        if (respostaIA == null || respostaIA.trim().equalsIgnoreCase("VAZIO") || respostaIA.trim().isEmpty()) {
            return "Sincronização com IA concluída. Nenhuma nova restrição foi adicionada.";
        }

        List<Restricao> novasRestricoes = Arrays.stream(respostaIA.split("\\R"))
                .map(String::trim)
                .filter(tipo -> !tipo.isEmpty() && !tiposExistentes.contains(tipo))

                .map(tipo -> Restricao.builder().tipo(tipo).build())
                .collect(Collectors.toList());

        if (novasRestricoes.isEmpty()) {
            return "Sincronização com IA concluída. Nenhuma nova restrição foi adicionada.";
        }

        restricaoRepository.saveAll(novasRestricoes);

        return String.format("Sincronização com IA concluída! %d novas restrições foram adicionadas.",
                novasRestricoes.size());
    }
}
