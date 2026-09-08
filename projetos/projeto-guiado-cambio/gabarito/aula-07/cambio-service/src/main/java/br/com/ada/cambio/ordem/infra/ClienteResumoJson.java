package br.com.ada.cambio.ordem.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * O JSON, cru, como o cliente-service devolve.
 *
 * <p>Mora em {@code infra} e o nome termina em {@code Json} de proposito: e o
 * formato do OUTRO, nao o nosso. Se o cliente-service renomear um campo, so
 * este arquivo e o Adapter mudam.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteResumoJson(Long id, String nome, String cpf) {
}
