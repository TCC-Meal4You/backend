package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VerificaEmailServiceTest {

    private final VerificaEmailService verificaEmailService = new VerificaEmailService();

    @Test
    void shouldValidateGeneratedCodeOnceAndRemoveItAfterUse() {
        String email = "verify@example.com";
        String codigo = verificaEmailService.gerarESalvarCodigo(email);

        assertThat(codigo).hasSize(6);
        assertThat(verificaEmailService.validarCodigo(email, codigo)).isTrue();
        assertThat(verificaEmailService.validarCodigo(email, codigo)).isFalse();
    }

    @Test
    void shouldRejectUnknownVerificationCode() {
        String email = "verify@example.com";
        verificaEmailService.gerarESalvarCodigo(email);

        assertThat(verificaEmailService.validarCodigo(email, "000000")).isFalse();
    }
}
