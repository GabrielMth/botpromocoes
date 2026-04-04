package br.com.offersbot.gateway.fetcher;

import br.com.offersbot.entity.Oferta;
import br.com.offersbot.gateway.mapper.MercadoLivreMapper;
import br.com.offersbot.usecase.port.out.BuscadorDeOfertasGateway;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MercadoLivreGateway implements BuscadorDeOfertasGateway {

    private static final String URL = "https://www.mercadolivre.com.br/ofertas";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final MercadoLivreMapper mapper;

    public MercadoLivreGateway(MercadoLivreMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Oferta> buscarOfertas() {
        try {
            Document document = Jsoup.connect(URL)
                    .userAgent(USER_AGENT)
                    .timeout(10_000)
                    .get();

            Elements cards = document.select("div.poly-card");
            System.out.println("Total de cards encontrados: " + cards.size());

            List<Oferta> ofertas = new ArrayList<>();
            for (var card : cards) {
                try {
                    Oferta oferta = mapper.mapear(card);
                    System.out.println("Oferta: " + oferta.titulo()
                            + " | Desconto: " + oferta.calcularDesconto() + "%");
                    if (!oferta.titulo().isBlank()) {
                        ofertas.add(oferta);
                    }
                } catch (Exception e) {
                    System.out.println("Erro ao mapear card: " + e.getMessage());
                }
            }

            System.out.println("Total de ofertas válidas: " + ofertas.size());
            return ofertas;

        } catch (IOException e) {
            System.out.println("Erro ao acessar Mercado Livre: " + e.getMessage());
            return List.of();
        }
    }








}