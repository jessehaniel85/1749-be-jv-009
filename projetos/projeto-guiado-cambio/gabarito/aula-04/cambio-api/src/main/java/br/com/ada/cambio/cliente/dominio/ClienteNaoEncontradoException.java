package br.com.ada.cambio.cliente.dominio;

/** Lançada quando não existe cliente para o CPF informado. Vira HTTP 404. */
public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(String cpf) {
        super("Cliente não encontrado para o CPF " + cpf);
    }
}
