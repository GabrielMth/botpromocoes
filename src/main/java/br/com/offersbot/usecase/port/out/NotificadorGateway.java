package br.com.offersbot.usecase.port.out;

import br.com.offersbot.entity.Oferta;

public interface NotificadorGateway {
    void enviar(Oferta oferta);
}
