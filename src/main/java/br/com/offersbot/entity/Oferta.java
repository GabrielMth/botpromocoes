package br.com.offersbot.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record Oferta(
        String id,
        String titulo,
        String urlProduto,
        String urlAfiliado,
        String imagemUrl,
        BigDecimal precoOriginal,
        BigDecimal precoAtual,
        Plataforma plataforma,
        DetalhesOferta detalhesOferta,
        LocalDateTime capturedAt
) {

    public BigDecimal calcularDesconto() {
        if (precoOriginal == null || precoOriginal.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        return precoOriginal.subtract(precoAtual)
                .divide(precoOriginal, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public boolean temDesconto() {
        return calcularDesconto().compareTo(BigDecimal.ZERO) > 0;
    }

}
