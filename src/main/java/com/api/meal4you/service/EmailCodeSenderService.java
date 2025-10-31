package com.api.meal4you.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class EmailCodeSenderService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    public void enviarEmail(String toEmail, String subject, String body) {
        Email from = new Email("meal4you.co@gmail.com");
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao enviar e-mail via SendGrid: " + ex.getMessage()
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro inesperado ao enviar e-mail: " + ex.getMessage()
            );
        }
    }
}
