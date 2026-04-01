package br.com.offersbot.usecase.port.out;

import br.com.offersbot.entity.Oferta;

public interface OfertaRepository {
    boolean jaFoiEnviada(String ofertaId);
    void salvar(Oferta oferta);
}
