package br.com.offersbot.gateway.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ofertas_enviadas")
public class OfertaEnviada {

    @Id
    private String id;
    private String plataforma;
    private LocalDateTime enviadoEm;

    public OfertaEnviada() {}

    public OfertaEnviada(String id, String plataforma, LocalDateTime enviadoEm) {
        this.id = id;
        this.plataforma = plataforma;
        this.enviadoEm = enviadoEm;
    }

    public String getId() { return id; }
    public String getPlataforma() { return plataforma; }
    public LocalDateTime getEnviadoEm() { return enviadoEm; }
}