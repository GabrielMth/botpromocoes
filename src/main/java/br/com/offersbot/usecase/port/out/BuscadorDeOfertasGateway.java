package br.com.offersbot.usecase.port.out;

import br.com.offersbot.entity.Oferta;

import java.util.List;

public interface BuscadorDeOfertasGateway {
    List<Oferta> buscarOfertas();
}
