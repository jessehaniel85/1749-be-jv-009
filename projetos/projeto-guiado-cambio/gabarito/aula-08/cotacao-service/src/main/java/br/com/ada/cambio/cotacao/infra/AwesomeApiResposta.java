package br.com.ada.cambio.cotacao.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Formato CRU devolvido pela awesomeapi para {@code /last/USD-BRL}:
 *
 * <pre>
 * { "USDBRL": { "code": "USD", "bid": "5.4321", "create_date": "2026-09-14 16:11:23" } }
 * </pre>
 *
 * <p>Este record existe SO para o Adapter. Nenhuma outra classe do sistema o enxerga.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AwesomeApiResposta(String code, String bid, String create_date) {
}
