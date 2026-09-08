package br.com.ada.cambio.ordem.dominio;

import java.util.Set;

/**
 * O catalogo de moedas que a instituicao negocia.
 *
 * <p>Existe em TRES implementacoes neste projeto, e isso e proposital — as duas
 * primeiras sao material didatico da Aula 8, a terceira e a que roda:</p>
 *
 * <ol>
 *   <li>{@link CatalogoMoedas} — Singleton classico, com {@code getInstance()};</li>
 *   <li>{@link CatalogoMoedasEnum} — Singleton baseado em {@code enum};</li>
 *   <li>{@link CatalogoMoedasBean} — bean Spring. <b>E esta que o fluxo real usa.</b></li>
 * </ol>
 *
 * <p>O porque de preferir a terceira esta em {@code docs/patterns-no-projeto.md}.</p>
 */
public interface CatalogoDeMoedas {

    /** Moedas negociadas, em ordem estavel. Colecao imutavel. */
    Set<Moeda> disponiveis();

    boolean suporta(String sigla);

    /**
     * Resolve a sigla recebida na requisicao.
     *
     * @throws MoedaNaoSuportadaException se a sigla nao estiver no catalogo
     */
    Moeda resolver(String sigla);
}
