package br.com.ada.cambio.cotacao.dominio;

/**
 * Contrato de quem sabe dizer a cotação de uma moeda.
 *
 * <p>Esta interface mora no <b>domínio</b>, e não na infra, de propósito: quem define o
 * contrato é quem precisa do serviço, não quem o implementa. É o "D" de SOLID
 * (Dependency Inversion) na prática — {@code CotacaoService} depende desta abstração e
 * não sabe se o número veio de uma tabela H2 ou de uma chamada HTTP a um provedor externo.</p>
 *
 * <p>Implementações (em {@code cotacao.infra}), escolhidas pela propriedade
 * {@code cotacao.provedor}:</p>
 * <ul>
 *   <li>{@code local} (padrão) — {@code CotacaoLocalProvider}, lê a tabela {@code cotacoes};</li>
 *   <li>{@code externo} — {@code CotacaoExternaProvider}, consulta a awesomeapi e traduz a
 *       resposta com o {@code AwesomeApiAdapter}.</li>
 * </ul>
 */
public interface CotacaoProvider {

    /**
     * Cotação vigente da moeda.
     *
     * @throws MoedaNaoSuportadaException se o provedor não conhece a moeda
     * @throws CotacaoIndisponivelException se o provedor existe mas não respondeu utilmente
     */
    Cotacao obter(Moeda moeda);
}
