package com.api.meal4you.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void shouldGenerateTokenAndExtractEmailAndRole() {
        String token = jwtUtil.gerarToken("user@example.com", "USUARIO");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.tokenValido(token)).isTrue();
        assertThat(jwtUtil.extrairEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtUtil.extrairRole(token)).isEqualTo("USUARIO");
        assertThat(jwtUtil.getExpiracao(token)).isAfterOrEqualTo(new java.util.Date());
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertThat(jwtUtil.tokenValido("invalid.token.value")).isFalse();
    }
}
