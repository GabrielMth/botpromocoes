package br.com.offersbot.gateway.notifier;

import br.com.offersbot.entity.Oferta;
import br.com.offersbot.entity.DetalhesMercadoLivre;
import br.com.offersbot.gateway.ai.GeradorDeMensagemGateway;
import br.com.offersbot.usecase.port.out.NotificadorGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class TelegramNotificador implements NotificadorGateway {

    private final TelegramClient telegramClient;
    private final String chatId;
    private final GeradorDeMensagemGateway geradorDeMensagem;

    public TelegramNotificador(
            @Value("${bot.telegram.token}") String token,
            @Value("${bot.telegram.chat-id}") String chatId,
            GeradorDeMensagemGateway geradorDeMensagem
    ) {
        this.telegramClient = new OkHttpTelegramClient(token);
        this.chatId = chatId;
        this.geradorDeMensagem = geradorDeMensagem;
    }

    @Override
    public void enviar(Oferta oferta) {
        try {
            String mensagem = formatarMensagem(oferta);

            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(oferta.imagemUrl()))
                    .caption(mensagem)
                    .parseMode("HTML")
                    .build();

            telegramClient.execute(sendPhoto);

            Thread.sleep(20 * 60 * 1000L);

        } catch (TelegramApiException e) {
            System.out.println("Erro ao enviar para Telegram: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
                    sb.append("⚡ <b>").append(badge).append("</b>\n\n")
            );
        }

        sb.append("🛍 <b>").append(oferta.titulo()).append("</b>\n\n");

        if (oferta.precoOriginal() != null) {
            sb.append("💰 <s>R$ ").append(formatarPreco(oferta.precoOriginal())).append("</s>\n");
            sb.append("✅ À VISTA: <b>R$ ").append(formatarPreco(oferta.precoAtual())).append("</b> - ");
            sb.append("🏷 <b>").append(oferta.calcularDesconto().intValue()).append("% OFF</b>\n");
        }

        sb.append("\n");
        sb.append("\n");
        if (oferta.detalhesOferta() instanceof DetalhesMercadoLivre ml) {

            if (ml.descontoPix().isPresent()) {
                sb.append("💳 Pix: <b>R$ ")
                        .append(formatarPreco(oferta.precoAtual()))
                        .append("</b> - <b>").append(ml.descontoPix().get()).append("</b>\n");

                if (ml.precoOutrosMeios().isPresent()) {
                    sb.append("💵 Outros meios: R$ ")
                            .append(ml.precoOutrosMeios().get());
                    ml.descontoOutrosMeios().ifPresent(d ->
                            sb.append(" - ").append(d)
                    );
                    sb.append("\n");
                }
            } else {

                sb.append("✅ <b>À VISTA: R$ ").append(formatarPreco(oferta.precoAtual())).append("</b>\n");
                sb.append("🏷 <b>").append(oferta.calcularDesconto().intValue()).append("% OFF</b>\n");
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

        sb.append("\n🔗 <a href=\"").append(oferta.urlAfiliado()).append("\">Ver oferta</a>");

        return sb.toString();
    }

    private String formatarPreco(java.math.BigDecimal preco) {
        if (preco == null) return "0,00";
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("pt", "BR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(preco);
    }

}