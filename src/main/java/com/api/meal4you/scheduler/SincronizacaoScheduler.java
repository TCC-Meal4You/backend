package com.api.meal4you.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.api.meal4you.service.RestricaoService;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor
public class SincronizacaoScheduler {

    private final RestricaoService restricaoService;

    @Scheduled(cron = "0 0 3 1 JAN,JUL ?")
    public void executarSincronizacaoAgendada() {
        restricaoService.sincronizarComIA();
    }
}
