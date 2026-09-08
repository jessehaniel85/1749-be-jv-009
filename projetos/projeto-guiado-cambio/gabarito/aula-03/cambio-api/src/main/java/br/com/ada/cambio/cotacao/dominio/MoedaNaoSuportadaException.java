package br.com.ada.cambio.cotacao.dominio;

/**
 * Lançada quando a sigla informada não é uma moeda operada pela mesa de câmbio.
 * Vira HTTP 422: a requisição está bem formada, mas o conteúdo não é processável.
 */
public class MoedaNaoSuportadaException extends RuntimeException {

    public MoedaNaoSuportadaException(String sigla) {
        super("Moeda não suportada: " + sigla + ". Moedas operadas: USD, EUR");
    }
}
