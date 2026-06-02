package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EmailCodeSenderServiceTest {

    @Mock
    private SendGrid sendGrid;

    @InjectMocks
    private EmailCodeSenderService emailCodeSenderService;

    @Test
    void shouldSendEmailSuccessfully() throws IOException {
        when(sendGrid.api(any(Request.class))).thenReturn(new Response(202, "Accepted", null));

        emailCodeSenderService.enviarEmail("user@example.com", "Assunto", "Corpo do email");

        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void shouldThrowServerErrorWhenSendGridReturnsNonAcceptedStatus() throws IOException {
        when(sendGrid.api(any(Request.class))).thenReturn(new Response(400, "Bad Request", null));

        assertThatThrownBy(() -> emailCodeSenderService.enviarEmail("user@example.com", "Assunto", "Corpo"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Erro ao enviar e-mail via SendGrid");
    }

    @Test
    void shouldThrowServerErrorWhenSendGridFails() throws IOException {
        doThrow(new IOException("timeout")).when(sendGrid).api(any(Request.class));

        assertThatThrownBy(() -> emailCodeSenderService.enviarEmail("user@example.com", "Assunto", "Corpo"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Erro ao enviar e-mail via SendGrid");
    }
}