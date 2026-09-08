package br.com.ada.cambio.cotacao;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ada.cambio.cotacao.dominio.CotacaoService;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Integracao: contexto real + H2 semeada pelo data.sql. */
@SpringBootTest
class CotacaoServiceApplicationTest {

    @Autowired
    private CotacaoService cotacaoService;

    @Test
    @DisplayName("data.sql semeia USD 5.4321 e EUR 6.5857")
    void sementeDoDataSql() {
        assertThat(cotacaoService.consultar(Moeda.USD).getValorCotacao()).isEqualByComparingTo("5.4321");
        assertThat(cotacaoService.consultar(Moeda.EUR).getValorCotacao()).isEqualByComparingTo("6.5857");
    }
}
