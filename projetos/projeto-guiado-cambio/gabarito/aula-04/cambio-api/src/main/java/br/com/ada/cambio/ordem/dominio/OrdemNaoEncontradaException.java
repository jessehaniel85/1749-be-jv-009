package br.com.ada.cambio.ordem.dominio;

/** Lançada quando não existe ordem de compra com o id informado. Vira HTTP 404. */
public class OrdemNaoEncontradaException extends RuntimeException {

    public OrdemNaoEncontradaException(Long id) {
        super("Ordem de compra não encontrada: " + id);
    }
}
