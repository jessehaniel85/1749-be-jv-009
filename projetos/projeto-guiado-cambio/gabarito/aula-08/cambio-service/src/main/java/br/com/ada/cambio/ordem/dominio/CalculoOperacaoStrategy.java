package br.com.ada.cambio.ordem.dominio;

import java.math.BigDecimal;

/**
 * STRATEGY — uma familia de algoritmos de calculo, um por moeda,
 * intercambiaveis em tempo de execucao.
 *
 * <p><b>Problema que resolve:</b> ate a Aula 7 o calculo era um so
 * ({@code valor x cotacao}). Quando o produto pediu spread diferente por moeda,
 * a saida obvia seria um {@code switch (moeda)} dentro da calculadora — e cada
 * moeda nova reabriria uma classe que ja funcionava (violacao do OCP).</p>
 *
 * <p><b>Como o Strategy resolve:</b> cada moeda ganha uma implementacao. Moeda
 * nova = arquivo novo + {@code @Component}. O Spring injeta a
 * {@code List<CalculoOperacaoStrategy>} e a
 * {@code EstrategiasDeCalculoConfig} monta o mapa. Nada existente muda.</p>
 *
 * <p><b>Contrato (LSP):</b></p>
 * <ul>
 *   <li>pre: valores nao nulos e positivos;</li>
 *   <li>pos: devolve o total em BRL com {@link RegrasDeCambio#ESCALA_MONETARIA}
 *       casas, arredondado por {@link RegrasDeCambio#ARREDONDAMENTO};</li>
 *   <li>{@link #moeda()} e estavel: sempre a mesma para a mesma instancia.</li>
 * </ul>
 */
public interface CalculoOperacaoStrategy {

    /** A moeda que esta estrategia sabe calcular. E a chave dela no mapa. */
    Moeda moeda();

    /**
     * Converte o valor em moeda estrangeira para reais, aplicando a regra
     * comercial da moeda.
     */
    BigDecimal calcular(BigDecimal valorMoedaEstrangeira, BigDecimal cotacao);
}
