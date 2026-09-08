package br.com.ada.cambio.cotacao.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Corpo de entrada de {@code PUT /cotacoes/{moeda}}. */
public record AtualizacaoCotacaoRequest(

        @NotNull(message = "valorCotacao e obrigatorio")
        @DecimalMin(value = "0.0001", message = "valorCotacao deve ser maior que zero")
        BigDecimal valorCotacao) {
}
