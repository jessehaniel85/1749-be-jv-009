package br.com.ada.cambio.cliente.dominio;

/** Nao existe cliente cadastrado com o CPF informado. Vira HTTP 404. */
public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(String cpf) {
        super("Cliente nao encontrado para o CPF " + cpf);
    }
}
