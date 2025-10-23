package com.api.meal4you.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailCodeSenderService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmail(String toEmail,
                          String subject,
                          String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("meal4you.co@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);

    }
}
