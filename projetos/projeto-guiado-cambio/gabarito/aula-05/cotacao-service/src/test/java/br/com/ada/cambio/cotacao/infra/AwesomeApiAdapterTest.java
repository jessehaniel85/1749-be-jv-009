package br.com.ada.cambio.cotacao.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoIndisponivelException;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** O Adapter e testavel sem rede: entra JSON externo, sai modelo interno. */
class AwesomeApiAdapterTest {

    private final AwesomeApiAdapter adapter = new AwesomeApiAdapter();

    @Test
    @DisplayName("converte o payload externo no modelo interno")
    void converteRespostaExterna() {
        AwesomeApiResposta externa = new AwesomeApiResposta("USD", "5.4321", "2026-09-14 16:11:23");

        Cotacao cotacao = adapter.paraCotacao(Moeda.USD, externa);

        assertThat(cotacao.getMoeda()).isEqualTo(Moeda.USD);
        assertThat(cotacao.getValorCotacao()).isEqualByComparingTo("5.4321");
        assertThat(cotacao.getDataHora()).isEqualTo(LocalDateTime.of(2026, 9, 14, 16, 11, 23));
    }

    @Test
    @DisplayName("data ilegivel nao derruba a conversao")
    void toleraDataInvalida() {
        AwesomeApiResposta externa = new AwesomeApiResposta("EUR", "6.5857", "ontem");

        assertThat(adapter.paraCotacao(Moeda.EUR, externa).getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("payload sem 'bid' vira CotacaoIndisponivelException")
    void payloadSemValor() {
        AwesomeApiResposta externa = new AwesomeApiResposta("USD", null, "2026-09-14 16:11:23");

        assertThatThrownBy(() -> adapter.paraCotacao(Moeda.USD, externa))
                .isInstanceOf(CotacaoIndisponivelException.class);
    }
}
