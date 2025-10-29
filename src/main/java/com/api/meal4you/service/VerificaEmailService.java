package com.api.meal4you.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class VerificaEmailService {

    // Armazena: código e o horário de expiração para cada e-mail
    private final Map<String, CodigoInfo> emailParaCodigo = new ConcurrentHashMap<>();

     //Classe interna para guardar o código e o horário de expiração
    private static class CodigoInfo {
        String codigo;
        long tempExpiracao;

        // Construtor da classe interna (CódigoInfo)
        CodigoInfo(String codigo, long tempExpiracao) {
            this.codigo = codigo;
            this.tempExpiracao = tempExpiracao;
        }
    }

    // Gera um código de 6 dígitos e vincula ao e-mail, com validade de apenas 5 minutos
    public String gerarESalvarCodigo(String email) {
        String codigo = gerarCodigoAleatorio();
        long expiracao = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutos
        emailParaCodigo.put(email, new CodigoInfo(codigo, expiracao));
        return codigo;
    }

    // Valida se o código enviado pelo usuário está correto e dentro do prazo
    public boolean validarCodigo(String email, String codigo) {
        CodigoInfo info = emailParaCodigo.get(email);
        if (info == null) return false;
        boolean valido = info.codigo.equals(codigo) && System.currentTimeMillis() <= info.tempExpiracao;
        if (valido) {
            // Remove o código após uso
            emailParaCodigo.remove(email);
        }
        return valido;
    }

    // Remove códigos expirados da lista "emailParaCodigo" automaticamente a cada 3 minutos
    @Scheduled(fixedRate = 3 * 60 * 1000)
    public void removerCodigosExpirados() {
        long agora = System.currentTimeMillis();
        emailParaCodigo.entrySet().removeIf(entry -> entry.getValue().tempExpiracao < agora);
    }

    // Gera um código aleatório de 6 dígitos, entre 100000 e 999999
    private String gerarCodigoAleatorio() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        int min = 100000;
        int max = 999999;
        int numeroAleatorio = random.nextInt(max - min + 1) + min;
        return String.valueOf(numeroAleatorio);
    }
}
