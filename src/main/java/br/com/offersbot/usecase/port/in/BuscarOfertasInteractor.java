package br.com.offersbot.usecase.port.in;

import br.com.offersbot.entity.Oferta;
import br.com.offersbot.usecase.port.in.BuscarOfertasUseCase;
import br.com.offersbot.usecase.port.out.BuscadorDeOfertasGateway;
import br.com.offersbot.usecase.port.out.NotificadorGateway;
import br.com.offersbot.usecase.port.out.OfertaRepository;

import java.util.List;

public class BuscarOfertasInteractor implements BuscarOfertasUseCase {

    private final List<BuscadorDeOfertasGateway> gateways;
    private final OfertaRepository repository;
    private final List<NotificadorGateway> notificadores;
    private final int descontoMinimo;

    public BuscarOfertasInteractor(
            List<BuscadorDeOfertasGateway> gateways,
            OfertaRepository repository,
            List<NotificadorGateway> notificadores,
            int descontoMinimo
    ) {
        this.gateways = gateways;
        this.repository = repository;
        this.notificadores = notificadores;
        this.descontoMinimo = descontoMinimo;
    }

    @Override
    public void executar() {
        gateways.stream()
                .flatMap(gateway -> gateway.buscarOfertas().stream())
                .filter(oferta -> !repository.jaFoiEnviada(oferta.id()))
                .filter(oferta -> oferta.calcularDesconto()
                        .intValue() >= descontoMinimo)
                .forEach(this::processarOferta);
    }

    private void processarOferta(Oferta oferta) {
        notificadores.forEach(notificador -> notificador.enviar(oferta));
        repository.salvar(oferta);
    }
}