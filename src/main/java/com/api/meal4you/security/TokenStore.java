package com.api.meal4you.security;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class TokenStore {

    private final JwtUtil jwtUtil;

    private final Map<String, Date> tokenParaExpiracao = new ConcurrentHashMap<>();
    private final Map<String, String> tokenParaUsuario = new ConcurrentHashMap<>();


    public void salvarToken(String token) {
        Date expiration = jwtUtil.getExpiracao(token);
        String email = jwtUtil.extrairEmail(token);
        tokenParaExpiracao.put(token, expiration);
        tokenParaUsuario.put(token, email);
    }

    public void removerToken(String token) {
        tokenParaExpiracao.remove(token);
        tokenParaUsuario.remove(token);
    }

    public boolean tokenEhRegistradoAtivo(String token) {
        Date exp = tokenParaExpiracao.get(token);
        return exp != null && exp.after(new Date());
    }

     public void removerTodosTokensDoUsuario(String email) {
        tokenParaUsuario.entrySet().removeIf(entry -> entry.getValue().equals(email));
        tokenParaExpiracao.entrySet().removeIf(entry -> {
            try {
                return jwtUtil.extrairEmail(entry.getKey()).equals(email);
            } catch (Exception e) {
                return true;
            }
        });
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24 * 365)
    public void removerTokensExpirados() {
        Date now = new Date();
        tokenParaExpiracao.entrySet().removeIf(entry -> entry.getValue().before(now));
        tokenParaUsuario.keySet().removeIf(token -> !tokenParaExpiracao.containsKey(token));
    }
}
