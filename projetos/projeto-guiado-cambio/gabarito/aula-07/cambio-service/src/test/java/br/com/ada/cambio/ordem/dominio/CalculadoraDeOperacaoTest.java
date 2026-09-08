package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculadoraDeOperacaoTest {

    private final CalculadoraDeOperacao calculadora = new CalculadoraDeOperacao();

    @Test
    @DisplayName("100 EUR a 6.5857 = 658.57")
    void calculaTotalDoContratoDoModulo() {
        BigDecimal total = calculadora.calcularTotal(
                new BigDecimal("100.00"), new BigDecimal("6.5857"));

        assertThat(total).isEqualByComparingTo("658.57");
        assertThat(total.scale()).isEqualTo(RegrasDeCambio.ESCALA_MONETARIA);
    }

    @Test
    @DisplayName("arredondamento bancario: metade vai para o vizinho PAR")
    void usaHalfEven() {
        // 1.005 -> 1.00 (o vizinho par e o 0), e nao 1.01 como no HALF_UP da escola
        assertThat(calculadora.calcularTotal(new BigDecimal("1"), new BigDecimal("1.005")))
                .isEqualByComparingTo("1.00");
        // 1.015 -> 1.02 (o vizinho par e o 2)
        assertThat(calculadora.calcularTotal(new BigDecimal("1"), new BigDecimal("1.015")))
                .isEqualByComparingTo("1.02");
    }
}
