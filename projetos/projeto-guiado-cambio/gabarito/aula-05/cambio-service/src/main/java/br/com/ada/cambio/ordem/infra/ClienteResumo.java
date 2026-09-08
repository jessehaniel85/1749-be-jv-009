package br.com.ada.cambio.ordem.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * O pedaco do cliente que a ordem precisa conhecer.
 *
 * <p>Nao e a entidade Cliente: e a leitura do contrato do cliente-service.
 * {@code ignoreUnknown} deixa o outro servico crescer sem quebrar este.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteResumo(Long id, String nome, String cpf) {
}
