package br.com.ada.cambio.ordem.dominio;

import java.util.Arrays;
import java.util.Optional;

/**
 * Moedas estrangeiras negociadas.
 *
 * <p>O enum aparece DUAS vezes no repositorio (aqui e no cotacao-service) de
 * proposito: microsservicos nao compartilham jar de dominio. O que os une e o
 * CONTRATO HTTP, nao uma classe comum.</p>
 */
public enum Moeda {
    USD,
    EUR;

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
