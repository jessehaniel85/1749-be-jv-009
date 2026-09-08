package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Logica pura, teste puro: sem Spring, sem mock, sem banco.
 * O catalogo entra pelo construtor — repare que da para trocar por outro
 * sem tocar nesta classe (o que um {@code getInstance()} interno impediria).
 */
class ValidadorDeOrdemTest {

    private final ValidadorDeOrdem validador = new ValidadorDeOrdem(new CatalogoMoedasBean());

    @Test
    @DisplayName("resolve as siglas do catalogo, inclusive em minusculo")
    void resolveSiglas() {
        assertThat(validador.moedaDe("USD")).isEqualTo(Moeda.USD);
        assertThat(validador.moedaDe("eur")).isEqualTo(Moeda.EUR);
    }

    @Test
    @DisplayName("sigla fora do catalogo estoura MoedaNaoSuportadaException")
    void recusaSiglaDesconhecida() {
        assertThatThrownBy(() -> validador.moedaDe("JPY"))
                .isInstanceOf(MoedaNaoSuportadaException.class);
    }

    @Test
    @DisplayName("agencia com 4 digitos passa")
    void aceitaAgenciaValida() {
        assertThatCode(() -> validador.validarAgencia("7057")).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "agencia \"{0}\" e recusada")
    @ValueSource(strings = {"705", "70570", "70a7", "", "    "})
    @DisplayName("qualquer coisa diferente de 4 digitos e recusada")
    void recusaAgenciaInvalida(String agencia) {
        assertThatThrownBy(() -> validador.validarAgencia(agencia))
                .isInstanceOf(AgenciaInvalidaException.class);
    }

    @Test
    @DisplayName("agencia nula e recusada sem NullPointerException")
    void recusaAgenciaNula() {
        assertThatThrownBy(() -> validador.validarAgencia(null))
                .isInstanceOf(AgenciaInvalidaException.class);
    }
}
