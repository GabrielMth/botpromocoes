package br.com.offersbot.gateway.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GeradorDeMensagemGateway {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json");

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    @Value("${bot.ai.api-key}")
    private String apiKey;

    public String gerarChamada(String tituloProduto) {
        try {
            String prompt = """
        Crie UMA ÚNICA frase curtíssima e animada para divulgar esse produto em um grupo de ofertas.
        Produto: %s
        
        Regras OBRIGATÓRIAS:
        - Retorne APENAS a frase, sem aspas, sem numeração, sem explicações
        - Máximo 10 palavras
        - Seja criatívo
        - Gatilhos para vendas
        - Anúncio de Produto.
        - Desenvolver interesse no cliente
        - Use linguagem informal e empolgante
        - Use apenas 1 emoji no final ou começo
        - Não mencione preço
        - Não use aspas, markdown ou pontuação extra
        - Use gírias brasileiras, exemplo Esse celular é top, aproveite o precinho!
        - Outro Exemplo: Pagando pouco por tecnologia de ponta!
        """.formatted(tituloProduto);

            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);

            JsonArray messages = new JsonArray();
            messages.add(message);

            JsonObject body = new JsonObject();
            body.addProperty("model", "llama-3.1-8b-instant");
            body.addProperty("max_tokens", 150);
            body.add("messages", messages);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(gson.toJson(body), JSON))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return "";

                JsonObject responseJson = gson.fromJson(
                        response.body().string(), JsonObject.class
                );

                return responseJson
                        .getAsJsonArray("choices")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString();
            }

        } catch (IOException e) {
            System.out.println("Erro ao gerar mensagem: " + e.getMessage());
            return "";
        }
    }
}