package br.com.ada.cambio.cotacao.dominio;

/** A fonte de cotacao nao respondeu ou nao tem valor para a moeda. Vira HTTP 503. */
public class CotacaoIndisponivelException extends RuntimeException {

    public CotacaoIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public CotacaoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
