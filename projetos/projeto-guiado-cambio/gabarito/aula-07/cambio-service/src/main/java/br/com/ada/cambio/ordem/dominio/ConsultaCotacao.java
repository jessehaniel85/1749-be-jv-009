package br.com.ada.cambio.ordem.dominio;

/**
 * Porta de saida para consultar cotacoes (DIP).
 *
 * <p><b>Pre-condicao:</b> {@code moeda} nao nula.<br>
 * <b>Pos-condicao:</b> devolve {@link CotacaoVigente} com valor positivo;
 * estoura {@link MoedaNaoSuportadaException} se a fonte recusar a moeda e
 * {@link ServicoIndisponivelException} se ela nao responder. Nunca devolve
 * {@code null}.</p>
 */
public interface ConsultaCotacao {

    CotacaoVigente vigente(Moeda moeda);
}
