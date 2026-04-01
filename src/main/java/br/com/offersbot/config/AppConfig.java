package br.com.offersbot.config;


import br.com.offersbot.usecase.interactor.BuscarOfertasInteractor;
import br.com.offersbot.usecase.port.in.BuscarOfertasUseCase;
import br.com.offersbot.usecase.port.out.BuscadorDeOfertasGateway;
import br.com.offersbot.usecase.port.out.NotificadorGateway;
import br.com.offersbot.usecase.port.out.OfertaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
public class AppConfig {

    @Value("${bot.filtro.desconto-minimo}")
    private int descontoMinimo;

    @Bean
    public BuscarOfertasUseCase buscarOfertasUseCase(
            List<BuscadorDeOfertasGateway> gateways,
            OfertaRepository repository,
            List<NotificadorGateway> notificadores
    ) {
        return new BuscarOfertasInteractor(
                gateways,
                repository,
                notificadores,
                descontoMinimo
        );
    }
}