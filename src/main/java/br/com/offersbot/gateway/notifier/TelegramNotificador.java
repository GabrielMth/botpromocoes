package br.com.offersbot.gateway.notifier;

import br.com.offersbot.entity.Oferta;
import br.com.offersbot.entity.DetalhesMercadoLivre;
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

    public TelegramNotificador(
            @Value("${bot.telegram.token}") String token,
            @Value("${bot.telegram.chat-id}") String chatId
    ) {
        this.telegramClient = new OkHttpTelegramClient(token);
        this.chatId = chatId;
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

        } catch (TelegramApiException e) {
            System.out.println("Erro ao enviar para Telegram: " + e.getMessage());
        }
    }

    private String formatarMensagem(Oferta oferta) {
        StringBuilder sb = new StringBuilder();

        if (oferta.detalhesOferta() instanceof DetalhesMercadoLivre ml) {
            ml.badgeOferta().ifPresent(badge ->
                    sb.append("⚡ <b>").append(badge).append("</b>\n\n")
            );
        }

        sb.append("🛍 <b>").append(oferta.titulo()).append("</b>\n\n");

        if (oferta.precoOriginal() != null) {
            sb.append("💰 <s>R$ ").append(oferta.precoOriginal()).append("</s>\n");
        }

        sb.append("✅ <b>R$ ").append(oferta.precoAtual()).append("</b>\n");
        sb.append("🏷 <b>").append(oferta.calcularDesconto().intValue()).append("% OFF</b>\n");

        if (oferta.detalhesOferta() instanceof DetalhesMercadoLivre ml) {
            ml.descontoMP().ifPresent(d ->
                    sb.append("💳 ").append(d).append("\n")
            );
            ml.cupom().ifPresent(c ->
                    sb.append("🎟 ").append(c).append("\n")
            );
            ml.parcelas().ifPresent(p ->
                    sb.append("💵 ").append(p).append("\n")
            );
            if (ml.freteGratis()) {
                sb.append("🚚 Frete grátis\n");
            }
            if (ml.fullMercadoLivre()) {
                sb.append("⚡ Enviado pelo FULL\n");
            }
            ml.entregaRapida().ifPresent(e ->
                    sb.append("📦 ").append(e).append("\n")
            );
            ml.vendedor().ifPresent(v ->
                    sb.append("🏪 ").append(v).append("\n")
            );
            ml.avaliacao().ifPresent(a ->
                    ml.totalAvaliacoes().ifPresent(t ->
                            sb.append("⭐ ").append(a).append(" ").append(t).append("\n")
                    )
            );
        }

        sb.append("\n🔗 <a href=\"").append(oferta.urlAfiliado()).append("\">Ver oferta</a>");

        return sb.toString();
    }
}