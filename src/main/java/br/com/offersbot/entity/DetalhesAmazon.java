package br.com.offersbot.entity;

import java.util.Optional;

public record DetalhesAmazon(

        Optional<String> badgePrime,
        Optional<String> cupom,
        Optional<String> parcelas,
        Optional<String> vendedor,
        Optional<String> avaliacao,
        Optional<String> totalAvaliacoes,
        boolean primeGratis,
        Optional<String> entregaPrime

) implements DetalhesOferta {
}