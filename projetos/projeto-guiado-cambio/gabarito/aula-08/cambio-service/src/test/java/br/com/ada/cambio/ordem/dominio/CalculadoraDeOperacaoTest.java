package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A calculadora nao calcula mais: ela ESCOLHE quem calcula. */
class CalculadoraDeOperacaoTest {

    private final CalculadoraDeOperacao calculadora = new CalculadoraDeOperacao(Map.of(
            Moeda.USD, new CalculoUsdStrategy(),
            Moeda.EUR, new CalculoEurStrategy()));

    @Test
    @DisplayName("despacha USD para a estrategia sem spread")
    void despachaUsd() {
        assertThat(calculadora.calcularTotal(Moeda.USD,
                new BigDecimal("100.00"), new BigDecimal("5.4321")))
                .isEqualByComparingTo("543.21");
    }

    @Test
    @DisplayName("despacha EUR para a estrategia com spread")
    void despachaEur() {
        assertThat(calculadora.calcularTotal(Moeda.EUR,
                new BigDecimal("100.00"), new BigDecimal("6.5857")))
                .isEqualByComparingTo("661.86");
    }

    @Test
    @DisplayName("moeda sem estrategia registrada estoura MoedaNaoSuportadaException")
    void moedaSemEstrategia() {
        CalculadoraDeOperacao incompleta =
                new CalculadoraDeOperacao(Map.of(Moeda.USD, new CalculoUsdStrategy()));

        assertThatThrownBy(() -> incompleta.calcularTotal(Moeda.EUR,
                BigDecimal.TEN, BigDecimal.ONE))
                .isInstanceOf(MoedaNaoSuportadaException.class);
    }
}
