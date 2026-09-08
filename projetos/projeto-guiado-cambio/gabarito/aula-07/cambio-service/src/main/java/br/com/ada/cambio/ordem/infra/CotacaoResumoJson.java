package br.com.ada.cambio.ordem.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** O JSON, cru, como o cotacao-service devolve. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CotacaoResumoJson(String moeda, BigDecimal valorCotacao, LocalDateTime dataHora) {
}
