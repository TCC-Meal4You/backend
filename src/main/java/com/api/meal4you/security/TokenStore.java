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

    private final Map<String, Date> validTokens = new ConcurrentHashMap<>();

    public void adicionarToken(String token) {
        Date expiration = jwtUtil.getExpiracao(token);
        validTokens.put(token, expiration);
    }

    public void removerToken(String token) {
        validTokens.remove(token);
    }

    public boolean contemToken(String token) {
        Date exp = validTokens.get(token);
        return exp != null && exp.after(new Date());
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24 * 365)
    public void removerTokensExpirados() {
        Date now = new Date();
        validTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}
