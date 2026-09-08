package br.com.ada.cambio.cotacao.dominio;

/**
 * Lançada quando o provedor de cotação existe mas não devolveu um valor utilizável
 * (fora do ar, resposta em formato inesperado, timeout). Vira HTTP 503.
 *
 * <p>É diferente de {@link MoedaNaoSuportadaException}: lá o pedido é que não faz sentido;
 * aqui o pedido faz sentido e <b>nós</b> é que não conseguimos atender agora.</p>
 */
public class CotacaoIndisponivelException extends RuntimeException {

    public CotacaoIndisponivelException(Moeda moeda, String motivo) {
        super("Cotação indisponível para " + moeda + ": " + motivo);
    }
}
