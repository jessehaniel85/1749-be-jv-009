package br.com.ada.cambio.cotacao.api;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Corpo de saida de {@code GET /cotacoes/{moeda}} e {@code PUT /cotacoes/{moeda}}. */
public record CotacaoResponse(Moeda moeda, BigDecimal valorCotacao, LocalDateTime dataHora) {

    public static CotacaoResponse de(Cotacao cotacao) {
        return new CotacaoResponse(cotacao.getMoeda(), cotacao.getValorCotacao(), cotacao.getDataHora());
    }
}
