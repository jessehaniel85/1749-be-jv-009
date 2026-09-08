package br.com.ada.cambio.ordem.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Leitura do contrato do cotacao-service. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CotacaoResumo(String moeda, BigDecimal valorCotacao, LocalDateTime dataHora) {
}
