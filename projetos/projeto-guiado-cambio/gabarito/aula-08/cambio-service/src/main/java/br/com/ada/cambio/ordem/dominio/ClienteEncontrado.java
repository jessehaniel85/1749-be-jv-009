package br.com.ada.cambio.ordem.dominio;

/**
 * O pedaco do cliente que o dominio de cambio precisa conhecer.
 *
 * <p>Mora em {@code dominio} - nao em {@code infra} - de proposito: e o dominio
 * quem define o que precisa receber. A infraestrutura que se vire para produzir
 * isto a partir do JSON que o cliente-service devolve (DIP).</p>
 */
public record ClienteEncontrado(Long id, String cpf, String nome) {
}
