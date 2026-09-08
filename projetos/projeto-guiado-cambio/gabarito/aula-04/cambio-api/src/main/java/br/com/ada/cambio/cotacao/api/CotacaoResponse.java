package br.com.ada.cambio.cotacao.api;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Representação da cotação devolvida pela API. */
public record CotacaoResponse(Moeda moeda, BigDecimal valorCotacao, LocalDateTime dataHora) {

    public static CotacaoResponse de(Cotacao cotacao) {
        return new CotacaoResponse(cotacao.getMoeda(), cotacao.getValorCotacao(), cotacao.getDataHora());
    }
}
