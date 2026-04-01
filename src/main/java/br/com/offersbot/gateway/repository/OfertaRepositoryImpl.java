package br.com.offersbot.gateway.repository;

import br.com.offersbot.entity.Oferta;
import br.com.offersbot.gateway.repository.entity.OfertaEnviada;
import br.com.offersbot.usecase.port.out.OfertaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OfertaRepositoryImpl implements OfertaRepository {

    private final OfertaJpaRepository jpaRepository;

    public OfertaRepositoryImpl(OfertaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean jaFoiEnviada(String ofertaId) {
        return jpaRepository.existsById(ofertaId);
    }

    @Override
    public void salvar(Oferta oferta) {
        OfertaEnviada enviada = new OfertaEnviada(
                oferta.id(),
                oferta.plataforma().name(),
                LocalDateTime.now()
        );
        jpaRepository.save(enviada);
    }
}