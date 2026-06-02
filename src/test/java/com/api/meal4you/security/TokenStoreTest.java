package com.api.meal4you.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.api.meal4you.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class TokenStoreTest extends BaseUnitTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenStore tokenStore;

    @Test
    void shouldRegisterActiveTokenAndKeepItActive() {
        String token = "active-token";
        Date expiration = new Date(System.currentTimeMillis() + 5_000);

        when(jwtUtil.getExpiracao(token)).thenReturn(expiration);
        when(jwtUtil.extrairEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extrairRole(token)).thenReturn("USUARIO");

        tokenStore.salvarToken(token);

        assertThat(tokenStore.tokenEhRegistradoAtivo(token)).isTrue();
    }

    @Test
    void shouldRemoveExpiredTokenWhenScheduledTaskRuns() {
        String token = "expired-token";
        Date expiration = new Date(System.currentTimeMillis() - 5_000);

        when(jwtUtil.getExpiracao(token)).thenReturn(expiration);
        when(jwtUtil.extrairEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extrairRole(token)).thenReturn("USUARIO");

        tokenStore.salvarToken(token);

        assertThat(tokenStore.tokenEhRegistradoAtivo(token)).isFalse();

        tokenStore.removerTokensExpirados();

        assertThat(tokenStore.tokenEhRegistradoAtivo(token)).isFalse();
    }

    @Test
    void shouldRemoveAllTokensForPersonWithSameEmailAndRole() {
        String tokenA = "token-A";
        String tokenB = "token-B";
        String tokenC = "token-C";
        Date expiration = new Date(System.currentTimeMillis() + 5_000);

        when(jwtUtil.getExpiracao(tokenA)).thenReturn(expiration);
        when(jwtUtil.extrairEmail(tokenA)).thenReturn("user@example.com");
        when(jwtUtil.extrairRole(tokenA)).thenReturn("USUARIO");

        when(jwtUtil.getExpiracao(tokenB)).thenReturn(expiration);
        when(jwtUtil.extrairEmail(tokenB)).thenReturn("user@example.com");
        when(jwtUtil.extrairRole(tokenB)).thenReturn("USUARIO");

        when(jwtUtil.getExpiracao(tokenC)).thenReturn(expiration);
        when(jwtUtil.extrairEmail(tokenC)).thenReturn("other@example.com");
        when(jwtUtil.extrairRole(tokenC)).thenReturn("USUARIO");

        tokenStore.salvarToken(tokenA);
        tokenStore.salvarToken(tokenB);
        tokenStore.salvarToken(tokenC);

        tokenStore.removerTodosTokensDaPessoa("user@example.com", "USUARIO");

        assertThat(tokenStore.tokenEhRegistradoAtivo(tokenA)).isFalse();
        assertThat(tokenStore.tokenEhRegistradoAtivo(tokenB)).isFalse();
        assertThat(tokenStore.tokenEhRegistradoAtivo(tokenC)).isTrue();
    }
}
