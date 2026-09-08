package br.com.ada.cambio.cliente.dominio;

/** Lançada quando já existe cliente com o CPF informado. Vira HTTP 409. */
public class CpfDuplicadoException extends RuntimeException {

    public CpfDuplicadoException(String cpf) {
        super("Já existe cliente cadastrado com o CPF " + cpf);
    }
}
