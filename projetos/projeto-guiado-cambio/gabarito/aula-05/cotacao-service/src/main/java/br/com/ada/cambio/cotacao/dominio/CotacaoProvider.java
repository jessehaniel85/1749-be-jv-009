package br.com.ada.cambio.cotacao.dominio;

/**
 * Contrato de servico da cotacao: o resto do sistema depende desta interface,
 * nunca de uma fonte concreta.
 *
 * <p>Duas implementacoes convivem e sao escolhidas pela propriedade
 * {@code cotacao.provedor}:</p>
 * <ul>
 *   <li>{@code local} (padrao) - tabela H2 do proprio servico;</li>
 *   <li>{@code externo} - awesomeapi, atras de um Adapter.</li>
 * </ul>
 */
public interface CotacaoProvider {

    /**
     * Devolve a cotacao vigente da moeda.
     *
     * @param moeda moeda desejada, nunca nula
     * @return cotacao com valor e instante preenchidos
     * @throws CotacaoIndisponivelException quando a fonte nao tem valor para a moeda
     */
    Cotacao obter(Moeda moeda);
}
