package br.com.ada.cambio.comum.api;

/** Um problema de validação em um campo específico do corpo da requisição. */
public record ErroDeCampo(String campo, String mensagem) {
}
