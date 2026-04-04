package br.com.offersbot.entity;

import java.util.Optional;

public record DetalhesMercadoLivre(
        Optional<String> badgeOferta,
        Optional<String> cupom,
        Optional<String> parcelas,
        Optional<String> vendedor,
        Optional<String> avaliacao,
        Optional<String> totalAvaliacoes,
        Optional<String> descontoMP,
        Optional<String> descontoPix,
        Optional<String> precoOutrosMeios,
        Optional<String> descontoOutrosMeios,
        boolean fullMercadoLivre,
        Optional<String> entregaRapida,
        boolean freteGratis
) implements DetalhesOferta {
}