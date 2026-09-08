package br.com.ada.cambio.ordem.dominio;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Regra comercial do EURO: conversao <b>com spread de 0,5%</b>.
 *
 * <p>O euro tem volume menor e custo de funding maior para a mesa, entao a
 * instituicao cobra um spread de {@value #SPREAD_PERCENTUAL_TEXTO}% sobre o
 * valor convertido.</p>
 *
 * <p><b>Ordem das operacoes importa:</b> multiplicamos, aplicamos o spread e
 * SO ENTAO arredondamos. Arredondar duas vezes (uma antes do spread, outra
 * depois) introduz um centavo de erro em algumas faixas de valor — e num
 * sistema financeiro esse centavo aparece na conciliacao.</p>
 *
 * <p>Exemplo do contrato do modulo: 100,00 EUR a 6,5857 =&gt; 658,5700 x 1,005
 * = 661,862850 =&gt; <b>661,86</b>.</p>
 */
@Component
public class CalculoEurStrategy implements CalculoOperacaoStrategy {

    /** 0,5% — fator multiplicador 1,005. */
    public static final BigDecimal FATOR_COM_SPREAD = new BigDecimal("1.005");

    static final String SPREAD_PERCENTUAL_TEXTO = "0,5";

    @Override
    public Moeda moeda() {
        return Moeda.EUR;
    }

    @Override
    public BigDecimal calcular(BigDecimal valorMoedaEstrangeira, BigDecimal cotacao) {
        return valorMoedaEstrangeira
                .multiply(cotacao)
                .multiply(FATOR_COM_SPREAD)
                .setScale(RegrasDeCambio.ESCALA_MONETARIA, RegrasDeCambio.ARREDONDAMENTO);
    }
}
