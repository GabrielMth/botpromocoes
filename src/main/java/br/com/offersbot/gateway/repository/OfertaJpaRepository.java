package br.com.offersbot.gateway.repository;

import br.com.offersbot.gateway.repository.entity.OfertaEnviada;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfertaJpaRepository extends JpaRepository<OfertaEnviada, String> {
}
