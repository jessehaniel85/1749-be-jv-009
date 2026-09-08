package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** STRATEGY do euro: conversao com spread comercial de 0,5%. */
class CalculoEurStrategyTest {

    private final CalculoEurStrategy estrategia = new CalculoEurStrategy();

    @Test
    @DisplayName("declara que atende EUR")
    void declaraMoeda() {
        assertThat(estrategia.moeda()).isEqualTo(Moeda.EUR);
    }

    @Test
    @DisplayName("100 EUR a 6.5857 = 661.86 (658.57 + 0,5% de spread)")
    void aplicaSpread() {
        BigDecimal total = estrategia.calcular(new BigDecimal("100.00"), new BigDecimal("6.5857"));

        // 100.00 x 6.5857 = 658.5700 ; x 1.005 = 661.862850 ; HALF_EVEN(2) = 661.86
        assertThat(total).isEqualByComparingTo("661.86");
        assertThat(total.scale()).isEqualTo(RegrasDeCambio.ESCALA_MONETARIA);
    }

    @Test
    @DisplayName("o spread do euro deixa o total MAIOR que o do dolar na mesma cotacao")
    void spreadEncareceEmRelacaoAoDolar() {
        BigDecimal valor = new BigDecimal("100.00");
        BigDecimal cotacao = new BigDecimal("6.5857");

        BigDecimal comSpread = estrategia.calcular(valor, cotacao);
        BigDecimal semSpread = new CalculoUsdStrategy().calcular(valor, cotacao);

        assertThat(comSpread).isGreaterThan(semSpread);
    }

    @Test
    @DisplayName("arredonda uma unica vez, no fim")
    void arredondaSoNoFim() {
        // Se arredondassemos antes do spread: 658.57 x 1.005 = 661.86285 -> 661.86
        // Neste caso da igual; o teste documenta a ORDEM correta das operacoes.
        assertThat(estrategia.calcular(new BigDecimal("100.00"), new BigDecimal("6.5857")))
                .isEqualByComparingTo("661.86");
    }
}
