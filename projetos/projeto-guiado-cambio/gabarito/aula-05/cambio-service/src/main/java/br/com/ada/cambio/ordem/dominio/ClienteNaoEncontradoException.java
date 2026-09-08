package br.com.ada.cambio.ordem.dominio;

/** O cliente-service respondeu 404 para o CPF informado. Vira HTTP 404 aqui tambem. */
public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(String cpf) {
        super("Cliente nao encontrado para o CPF " + cpf);
    }
}
