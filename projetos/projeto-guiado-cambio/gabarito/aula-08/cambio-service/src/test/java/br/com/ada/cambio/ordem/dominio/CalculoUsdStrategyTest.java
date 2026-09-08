package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** STRATEGY do dolar: conversao direta, sem spread. */
class CalculoUsdStrategyTest {

    private final CalculoUsdStrategy estrategia = new CalculoUsdStrategy();

    @Test
    @DisplayName("declara que atende USD")
    void declaraMoeda() {
        assertThat(estrategia.moeda()).isEqualTo(Moeda.USD);
    }

    @Test
    @DisplayName("100 USD a 5.4321 = 543.21, sem spread")
    void calculaSemSpread() {
        BigDecimal total = estrategia.calcular(new BigDecimal("100.00"), new BigDecimal("5.4321"));

        assertThat(total).isEqualByComparingTo("543.21");
        assertThat(total.scale()).isEqualTo(RegrasDeCambio.ESCALA_MONETARIA);
    }

    @Test
    @DisplayName("usa arredondamento bancario (HALF_EVEN)")
    void usaHalfEven() {
        // 1 x 1.005 = 1.005 -> 1.00 (vizinho par), e nao 1.01 do HALF_UP
        assertThat(estrategia.calcular(BigDecimal.ONE, new BigDecimal("1.005")))
                .isEqualByComparingTo("1.00");
    }
}
