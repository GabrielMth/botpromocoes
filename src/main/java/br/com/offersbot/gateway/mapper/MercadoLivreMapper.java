package br.com.offersbot.gateway.mapper;

import br.com.offersbot.entity.DetalhesMercadoLivre;
import br.com.offersbot.entity.DetalhesOferta;
import br.com.offersbot.entity.Oferta;
import br.com.offersbot.entity.Plataforma;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class MercadoLivreMapper {

    private static final String MATT_TOOL = "73548021";
    private static final String MATT_WORD = "gabrielsantos230";

    public Oferta mapear(Element card) {
        String titulo = extrairTitulo(card);
        String urlProduto = extrairUrl(card);
        String imagemUrl = extrairImagem(card);
        BigDecimal precoOriginal = extrairPrecoOriginal(card);
        BigDecimal precoAtual = extrairPrecoAtual(card);
        String urlAfiliado = gerarUrlAfiliado(urlProduto);
        DetalhesOferta detalhes = extrairDetalhes(card);

        return new Oferta(
                extrairId(urlProduto),
                titulo,
                urlProduto,
                urlAfiliado,
                imagemUrl,
                precoOriginal,
                precoAtual,
                Plataforma.MERCADO_LIVRE,
                detalhes,
                LocalDateTime.now()
        );
    }



    private String extrairTitulo(Element card) {
        Element el = card.selectFirst("a.poly-component__title");
        return el != null ? el.text() : "";
    }

    private String extrairUrl(Element card) {
        Element el = card.selectFirst("a.poly-component__title");
        return el != null ? el.attr("href") : "";
    }

    private String extrairImagem(Element card) {
        Element el = card.selectFirst("img.poly-component__picture");
        return el != null ? el.attr("src") : "";
    }

    private BigDecimal extrairPrecoOriginal(Element card) {
        Element el = card.selectFirst("s.andes-money-amount--previous");
        if (el == null) return BigDecimal.ZERO;
        return parsearPreco(el.attr("aria-label"));
    }

    private BigDecimal extrairPrecoAtual(Element card) {
        Element el = card.selectFirst("div.poly-price__current span[aria-label]");
        if (el == null) return BigDecimal.ZERO;
        return parsearPreco(el.attr("aria-label"));
    }

    private BigDecimal parsearPreco(String ariaLabel) {
        if (ariaLabel == null || ariaLabel.isBlank()) return BigDecimal.ZERO;
        try {
            String valor = ariaLabel
                    .replace("Antes: ", "")
                    .replace("Agora: ", "")
                    .replace("reais com ", ".")
                    .replace(" centavos", "")
                    .replace("reais", "")
                    .replace(".", "")
                    .replace(",", ".")
                    .replaceAll("[^\\d.]", "")
                    .trim();
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private DetalhesMercadoLivre extrairDetalhes(Element card) {
        return new DetalhesMercadoLivre(
                extrairTexto(card, "span.poly-component__highlight"),
                extrairTexto(card, "span.poly-coupons__pill"),
                extrairTexto(card, "span.poly-price__installments"),
                extrairTexto(card, "span.poly-component__seller"),
                extrairTexto(card, "span.poly-reviews__rating"),
                extrairTexto(card, "span.poly-reviews__total"),
                extrairTexto(card, "span.poly-rebates__pill"),
                card.selectFirst("svg[aria-label='Enviado pelo FULL']") != null,
                extrairTexto(card, "span.poly-shipping--next_day"),
                extrairTexto(card, "span.poly-component__shipping span").isPresent()
        );
    }

    private Optional<String> extrairTexto(Element card, String seletor) {
        Element el = card.selectFirst(seletor);
        if (el == null || el.text().isBlank()) return Optional.empty();
        return Optional.of(el.text());
    }

    private String gerarUrlAfiliado(String urlProduto) {
        if (urlProduto == null || urlProduto.isBlank()) return "";
        String separador = urlProduto.contains("?") ? "&" : "?";
        return urlProduto + separador
                + "matt_word=" + MATT_WORD
                + "&matt_tool=" + MATT_TOOL;
    }

    private String extrairId(String url) {
        if (url == null || url.isBlank()) return UUID.randomUUID().toString();
        try {
            String[] partes = url.split("/");
            for (String parte : partes) {
                if (parte.startsWith("MLB")) return parte.split("\\?")[0];
            }
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
        return UUID.randomUUID().toString();
    }
}
