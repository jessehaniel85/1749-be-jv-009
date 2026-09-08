package br.com.ada.cambio.ordem.dominio;

/** Lançada quando o número da agência de retirada não tem 4 dígitos. Vira HTTP 422. */
public class AgenciaInvalidaException extends RuntimeException {

    public AgenciaInvalidaException(String numeroAgencia) {
        super("Agência de retirada inválida: " + numeroAgencia + ". Informe 4 dígitos numéricos");
    }
}
