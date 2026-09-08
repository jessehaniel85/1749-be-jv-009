package br.com.ada.cambio.cotacao.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoedaTest {

    @Test
    @DisplayName("aceita sigla em minusculo e com espacos")
    void aceitaSiglaNormalizada() {
        assertThat(Moeda.deSigla(" usd ")).contains(Moeda.USD);
    }

    @Test
    @DisplayName("nao resolve sigla fora do catalogo")
    void recusaSiglaDesconhecida() {
        assertThat(Moeda.deSigla("JPY")).isEmpty();
        assertThat(Moeda.deSigla(null)).isEmpty();
    }
}
