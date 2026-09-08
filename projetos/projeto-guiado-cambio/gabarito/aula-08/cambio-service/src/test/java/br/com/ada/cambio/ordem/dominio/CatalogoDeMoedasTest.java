package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SINGLETON — as tres implementacoes tem o MESMO comportamento; o que muda e o
 * ciclo de vida e a testabilidade.
 */
class CatalogoDeMoedasTest {

    @Test
    @DisplayName("getInstance() devolve sempre a mesma instancia (singleton classico)")
    void singletonClassicoEhUnico() {
        assertThat(CatalogoMoedas.getInstance()).isSameAs(CatalogoMoedas.getInstance());
    }

    @Test
    @DisplayName("o enum tem uma unica constante, logo uma unica instancia")
    void singletonEnumEhUnico() {
        assertThat(CatalogoMoedasEnum.values()).hasSize(1);
        assertThat(CatalogoMoedasEnum.INSTANCIA).isSameAs(CatalogoMoedasEnum.valueOf("INSTANCIA"));
    }

    @Test
    @DisplayName("as tres implementacoes concordam sobre o catalogo")
    void mesmoComportamento() {
        for (CatalogoDeMoedas catalogo : new CatalogoDeMoedas[]{
                CatalogoMoedas.getInstance(), CatalogoMoedasEnum.INSTANCIA, new CatalogoMoedasBean()}) {

            assertThat(catalogo.disponiveis()).containsExactlyInAnyOrder(Moeda.USD, Moeda.EUR);
            assertThat(catalogo.suporta("usd")).isTrue();
            assertThat(catalogo.suporta("JPY")).isFalse();
            assertThat(catalogo.resolver("eur")).isEqualTo(Moeda.EUR);
            assertThatThrownBy(() -> catalogo.resolver("JPY"))
                    .isInstanceOf(MoedaNaoSuportadaException.class);
        }
    }

    @Test
    @DisplayName("o catalogo devolvido e imutavel")
    void catalogoImutavel() {
        assertThatThrownBy(() -> new CatalogoMoedasBean().disponiveis().add(Moeda.USD))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
