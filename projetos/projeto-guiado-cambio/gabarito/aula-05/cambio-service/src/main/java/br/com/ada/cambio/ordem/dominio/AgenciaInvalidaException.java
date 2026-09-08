package br.com.ada.cambio.ordem.dominio;

/** Numero de agencia fora do formato de 4 digitos. Vira HTTP 422. */
public class AgenciaInvalidaException extends RuntimeException {

    public AgenciaInvalidaException(String numeroAgencia) {
        super("Numero de agencia invalido: '" + numeroAgencia + "'. Esperado: 4 digitos.");
    }
}
