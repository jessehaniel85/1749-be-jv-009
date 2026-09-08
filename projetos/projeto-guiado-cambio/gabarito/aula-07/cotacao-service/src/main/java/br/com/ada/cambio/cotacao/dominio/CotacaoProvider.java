package br.com.ada.cambio.cotacao.dominio;

/**
 * Contrato de servico da cotacao. O resto do sistema depende desta interface,
 * nunca de uma fonte concreta.
 *
 * <h2>OCP — aberto para extensao, fechado para modificacao</h2>
 * <p>Uma fonte nova (Banco Central, cache, mock de teste) entra como uma
 * implementacao a mais. Nenhuma linha de {@link CotacaoService},
 * {@code CotacaoController} ou dos testes precisa mudar. O sinal de que o OCP
 * esta valendo: <b>a palavra {@code if} nunca aparece perguntando "qual
 * provedor e este?"</b> — quem decide e o Spring, pela propriedade
 * {@code cotacao.provedor}.</p>
 *
 * <h2>LSP — o contrato abaixo vale para TODA implementacao</h2>
 * <p>Substituir uma implementacao por outra nao pode surpreender quem chama.
 * Por isso o contrato esta escrito aqui, e nao "combinado" caso a caso:</p>
 *
 * <p><b>Pre-condicoes</b> (o chamador garante):</p>
 * <ul>
 *   <li>{@code moeda} nao e {@code null} e pertence ao enum {@link Moeda};</li>
 *   <li>a chamada pode ocorrer em qualquer thread (implementacoes devem ser
 *       seguras para uso concorrente — beans singleton do Spring o sao).</li>
 * </ul>
 *
 * <p><b>Pos-condicoes</b> (a implementacao garante):</p>
 * <ul>
 *   <li>devolve uma {@link Cotacao} nao nula, com {@code moeda} igual a pedida;</li>
 *   <li>{@code valorCotacao} e estritamente positivo, com ate
 *       {@link Cotacao#ESCALA_COTACAO} casas decimais;</li>
 *   <li>{@code dataHora} e o instante da apuracao — nunca {@code null};</li>
 *   <li>a instancia devolvida <b>pode nao estar persistida</b> (o provedor
 *       externo devolve um objeto transiente). Quem chama nao deve assumir
 *       {@code id} preenchido;</li>
 *   <li>falha de fonte vira {@link CotacaoIndisponivelException} — e <b>nunca</b>
 *       {@code null}, nunca uma excecao especifica de biblioteca, nunca um valor
 *       "zero" de consolacao.</li>
 * </ul>
 *
 * <p>Esta ultima linha e o coracao do LSP: uma implementacao que devolvesse
 * {@code null} em vez de estourar obrigaria todo chamador a saber QUAL
 * implementacao esta ativa — e ai a abstracao ja teria morrido.</p>
 *
 * <h2>Implementacoes</h2>
 * <ul>
 *   <li>{@code CotacaoLocalProvider} — tabela H2 do proprio servico
 *       ({@code cotacao.provedor=local}, padrao);</li>
 *   <li>{@code CotacaoExternaProvider} — awesomeapi atras de um Adapter
 *       ({@code cotacao.provedor=externo}).</li>
 * </ul>
 */
public interface CotacaoProvider {

    /**
     * Devolve a cotacao vigente da moeda.
     *
     * @param moeda moeda desejada, nunca nula
     * @return cotacao com valor positivo e instante preenchidos, nunca nula
     * @throws CotacaoIndisponivelException quando a fonte nao responde ou nao
     *         tem valor para a moeda
     */
    Cotacao obter(Moeda moeda);
}
