package br.com.ada.cambio.ordem.dominio;

/** Sigla de moeda fora do catalogo (USD, EUR). Vira HTTP 422. */
public class MoedaNaoSuportadaException extends RuntimeException {

    public MoedaNaoSuportadaException(String sigla) {
        super("Moeda nao suportada: " + sigla + ". Moedas disponiveis: USD, EUR");
    }
}
