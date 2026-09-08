package br.com.ada.cambio.ordem.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A cotacao como o dominio de cambio a enxerga. */
public record CotacaoVigente(Moeda moeda, BigDecimal valorCotacao, LocalDateTime dataHora) {
}
