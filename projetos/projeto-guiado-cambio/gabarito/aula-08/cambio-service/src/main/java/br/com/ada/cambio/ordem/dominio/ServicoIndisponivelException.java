package br.com.ada.cambio.ordem.dominio;

/**
 * Um servico do qual dependemos nao respondeu (fora do ar, timeout, erro 5xx ou
 * sem instancia registrada). Vira HTTP 503.
 */
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String servico, Throwable causa) {
        super("Servico indisponivel no momento: " + servico
                + ". Verifique se ele esta no ar e registrado, e tente novamente.", causa);
    }
}
