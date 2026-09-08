package br.com.ada.cambio.cotacao.dominio;

import java.util.Arrays;
import java.util.Optional;

/** Moedas estrangeiras negociadas pela instituicao. */
public enum Moeda {
    USD,
    EUR;

    /** Converte a sigla recebida na URL, sem estourar excecao. */
    public static Optional<Moeda> deSigla(String sigla) {
        if (sigla == null) {
            return Optional.empty();
        }
        String normalizada = sigla.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(moeda -> moeda.name().equals(normalizada))
                .findFirst();
    }
}
