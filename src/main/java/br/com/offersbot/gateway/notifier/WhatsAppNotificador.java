package br.com.offersbot.gateway.notifier;

import br.com.offersbot.entity.Oferta;
import br.com.offersbot.entity.DetalhesMercadoLivre;
import br.com.offersbot.gateway.ai.GeradorDeMensagemGateway;
import br.com.offersbot.usecase.port.out.NotificadorGateway;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WhatsAppNotificador implements NotificadorGateway {

    private static final MediaType JSON = MediaType.get("application/json");
    private final GeradorDeMensagemGateway geradorDeMensagem;

    private final OkHttpClient client = new OkHttpClient();

    @Value("${bot.whatsapp.url}")
    private String evolutionUrl;

    @Value("${bot.whatsapp.api-key}")
    private String apiKey;

    @Value("${bot.whatsapp.instance}")
    private String instance;

    @Value("${bot.whatsapp.group-id}")
    private String groupId;

    public WhatsAppNotificador(GeradorDeMensagemGateway geradorDeMensagem) {
        this.geradorDeMensagem = geradorDeMensagem;
    }

    @Override
    public void enviar(Oferta oferta) {
        System.out.println("WhatsApp notificador chamado para: " + oferta.titulo());
        try {
            enviarImagem(oferta);
        } catch (Exception e) {
            System.out.println("Erro ao enviar WhatsApp: " + e.getMessage());
        }
    }

    private void enviarImagem(Oferta oferta) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("number", groupId);
        body.addProperty("mediatype", "image");
        body.addProperty("mimetype", "image/jpeg");
        body.addProperty("media", oferta.imagemUrl());
        body.addProperty("caption", formatarMensagem(oferta));
        body.addProperty("fileName", "oferta.jpg");

        Request request = new Request.Builder()
                .url(evolutionUrl + "/message/sendMedia/" + instance)
                .post(RequestBody.create(body.toString(), JSON))
                .addHeader("apikey", apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "sem body";
                System.out.println("Erro ao enviar imagem WhatsApp: " + response.code() + " - " + responseBody);
            }
        }
    }

    private void enviarTexto(String mensagem) throws IOException {
        String json = "{\"number\":\"" + groupId + "\",\"text\":" +
                new com.google.gson.Gson().toJson(mensagem) + "}";

        System.out.println("JSON enviado: " + json);

        Request request = new Request.Builder()
                .url(evolutionUrl + "/message/sendText/" + instance)
                .post(RequestBody.create(json, JSON))
                .addHeader("apikey", apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "sem body";
                System.out.println("Erro ao enviar texto WhatsApp: " + response.code() + " - " + responseBody);
            }
        }
    }


    private String formatarMensagem(Oferta oferta) {
        StringBuilder sb = new StringBuilder();

        String chamada = geradorDeMensagem.gerarChamada(oferta.titulo());
        if (!chamada.isBlank()) {
            sb.append(chamada).append("\n\n");
        }

        if (oferta.detalhesOferta() instanceof DetalhesMercadoLivre ml) {
            ml.badgeOferta().ifPresent(badge ->
                    sb.append("⚡ *").append(badge).append("*\n\n")
            );
        }

        sb.append("🛍 *").append(oferta.titulo()).append("*\n\n");

        if (oferta.precoOriginal() != null) {
            sb.append("💰 ~R$ ").append(formatarPreco(oferta.precoOriginal())).append("~\n");
            sb.append("✅ À VISTA: *R$ ").append(formatarPreco(oferta.precoAtual())).append("* - ");
            sb.append("🏷 *").append(oferta.calcularDesconto().intValue()).append("% OFF*\n");
        }

        sb.append("\n\n");

        if (oferta.detalhesOferta() instanceof DetalhesMercadoLivre ml) {

            if (ml.descontoPix().isPresent()) {
                sb.append("💳 Pix: *R$ ")
                        .append(formatarPreco(oferta.precoAtual()))
                        .append("* - *").append(ml.descontoPix().get()).append("*\n");

                if (ml.precoOutrosMeios().isPresent()) {
                    sb.append("💵 Outros meios: R$ ")
                            .append(ml.precoOutrosMeios().get());
                    ml.descontoOutrosMeios().ifPresent(d ->
                            sb.append(" - ").append(d)
                    );
                    sb.append("\n");
                }
            } else {
                sb.append("✅ *À VISTA: R$ ").append(formatarPreco(oferta.precoAtual())).append("*\n");
                sb.append("🏷 *").append(oferta.calcularDesconto().intValue()).append("% OFF*\n");
            }

            ml.descontoMP().ifPresent(d ->
                    sb.append("🏦 ").append(d).append("\n")
            );
            ml.cupom().ifPresent(c ->
                    sb.append("🎟 ").append(c).append("\n")
            );
            ml.parcelas().ifPresent(p ->
                    sb.append("📊 ").append(p).append("\n")
            );
            if (ml.freteGratis()) {
                sb.append("🚚 Frete GRÁTIS\n");
            }
            if (ml.fullMercadoLivre()) {
                sb.append("⚡ ENTREGA FULL, CHEGA 1-3 dias\n");
            }
            ml.entregaRapida().ifPresent(e ->
                    sb.append("📦 ").append(e).append("\n")
            );
        }

        sb.append("\n🔗 ").append(encurtarUrl(oferta.urlAfiliado()));

        return sb.toString();
    }

    private String formatarPreco(java.math.BigDecimal preco) {
        if (preco == null) return "0,00";
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("pt", "BR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(preco);
    }


    private String encurtarUrl(String url) {
        try {
            String apiUrl = "https://tinyurl.com/api-create.php?url=" +
                    java.net.URLEncoder.encode(url, "UTF-8");

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao encurtar URL: " + e.getMessage());
        }
        return url;
    }
}
