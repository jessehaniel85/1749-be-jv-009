package br.com.ada.cambio.ordem.dominio;

/**
 * Um servico do qual dependemos nao respondeu (fora do ar, timeout ou erro 5xx).
 * Vira HTTP 503 - e o preco de ter quebrado o monolito.
 */
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String servico, Throwable causa) {
        super("Servico indisponivel no momento: " + servico
                + ". Verifique se ele esta no ar e tente novamente.", causa);
    }
}
