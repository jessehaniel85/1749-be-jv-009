package br.com.ada.cambio.ordem.dominio;

/** Nao existe ordem com o id informado. Vira HTTP 404. */
public class OrdemNaoEncontradaException extends RuntimeException {

    public OrdemNaoEncontradaException(Long id) {
        super("Ordem de compra nao encontrada: " + id);
    }
}
