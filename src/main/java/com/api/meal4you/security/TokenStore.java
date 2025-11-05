package com.api.meal4you.security;

import lombok.AllArgsConstructor;
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

    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    @AllArgsConstructor
    private static class TokenInfo {
        String email;
        String role;
        Date expiracao;
    }

    public void salvarToken(String token) {
        Date expiration = jwtUtil.getExpiracao(token);
        String email = jwtUtil.extrairEmail(token);
        String role = jwtUtil.extrairRole(token);
        tokens.put(token, new TokenInfo(email, role, expiration));
    }

    public void removerToken(String token) {
        tokens.remove(token);
    }

    public boolean tokenEhRegistradoAtivo(String token) {
        TokenInfo info = tokens.get(token);
        return info != null && info.expiracao.after(new Date());
    }

    public void removerTodosTokensDaPessoa(String email, String role) {
        tokens.entrySet().removeIf(entry -> {
            TokenInfo info = entry.getValue();
            return info.email.equals(email) && info.role.equals(role);
        });
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24 * 365)
    public void removerTokensExpirados() {
        Date now = new Date();
        tokens.entrySet().removeIf(entry -> entry.getValue().expiracao.before(now));
    }
}
