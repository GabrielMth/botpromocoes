package br.com.offersbot.config;

import br.com.offersbot.usecase.port.in.BuscarOfertasUseCase;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class Scheduler {

    private final BuscarOfertasUseCase buscarOfertasUseCase;

    public Scheduler(BuscarOfertasUseCase buscarOfertasUseCase) {
        this.buscarOfertasUseCase = buscarOfertasUseCase;
    }

    @Scheduled(fixedDelayString = "${bot.scheduler.intervalo}000")
    public void executar() {
        System.out.println("Iniciando busca de ofertas...");
        buscarOfertasUseCase.executar();
        System.out.println("Busca finalizada.");
    }
}