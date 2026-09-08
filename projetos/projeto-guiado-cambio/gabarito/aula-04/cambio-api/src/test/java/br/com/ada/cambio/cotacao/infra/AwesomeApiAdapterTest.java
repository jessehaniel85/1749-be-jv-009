package br.com.ada.cambio.cotacao.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoIndisponivelException;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Teste do Adapter — <b>sem rede</b>.
 *
 * <p>Este é o ponto do desenho: a tradução do modelo externo é uma função pura sobre um
 * objeto Java. Dá para testar todos os casos esquisitos do provedor (campo faltando, valor
 * podre, data em outro formato) sem depender de a awesomeapi estar no ar — e sem esperar
 * 3 segundos de timeout a cada execução.</p>
 */
class AwesomeApiAdapterTest {

    private final AwesomeApiAdapter adaptador = new AwesomeApiAdapter();

    @Test
    @DisplayName("converte a resposta da awesomeapi na cotação do domínio")
    void converteRespostaValida() {
        Map<String, AwesomeApiCotacao> resposta = Map.of("USDBRL",
                new AwesomeApiCotacao("USD", "BRL", "5.4321", "2026-09-08 10:31:02"));

        Cotacao cotacao = adaptador.paraCotacao(Moeda.USD, resposta);

        assertThat(cotacao.getMoeda()).isEqualTo(Moeda.USD);
        assertThat(cotacao.getValorCotacao()).isEqualByComparingTo("5.4321");
        assertThat(cotacao.getValorCotacao().scale()).isEqualTo(Cotacao.ESCALA_COTACAO);
        assertThat(cotacao.getDataHora()).isEqualTo(LocalDateTime.of(2026, 9, 8, 10, 31, 2));
    }

    @Test
    @DisplayName("normaliza o valor para 4 casas decimais")
    void normalizaAEscalaDoValor() {
        Map<String, AwesomeApiCotacao> resposta = Map.of("EURBRL",
                new AwesomeApiCotacao("EUR", "BRL", "6.58", "2026-09-08 10:31:02"));

        Cotacao cotacao = adaptador.paraCotacao(Moeda.EUR, resposta);

        assertThat(cotacao.getValorCotacao().toPlainString()).isEqualTo("6.5800");
    }

    @Test
    @DisplayName("data em formato inesperado não derruba a conversão — assume o momento da leitura")
    void dataInvalidaViraAgora() {
        Map<String, AwesomeApiCotacao> resposta = Map.of("USDBRL",
                new AwesomeApiCotacao("USD", "BRL", "5.4321", "ontem de tarde"));

        Cotacao cotacao = adaptador.paraCotacao(Moeda.USD, resposta);

        assertThat(cotacao.getDataHora()).isNotNull();
        assertThat(cotacao.getValorCotacao()).isEqualByComparingTo("5.4321");
    }

    @Test
    @DisplayName("resposta sem o par pedido vira CotacaoIndisponivelException")
    void respostaSemOParPedido() {
        Map<String, AwesomeApiCotacao> resposta = Map.of("EURBRL",
                new AwesomeApiCotacao("EUR", "BRL", "6.5857", "2026-09-08 10:31:02"));

        assertThatThrownBy(() -> adaptador.paraCotacao(Moeda.USD, resposta))
                .isInstanceOf(CotacaoIndisponivelException.class)
                .hasMessageContaining("USDBRL");
    }

    @Test
    @DisplayName("resposta vazia vira CotacaoIndisponivelException")
    void respostaVazia() {
        assertThatThrownBy(() -> adaptador.paraCotacao(Moeda.USD, Map.of()))
                .isInstanceOf(CotacaoIndisponivelException.class);
    }

    @Test
    @DisplayName("valor em formato inesperado vira CotacaoIndisponivelException")
    void valorIlegivel() {
        Map<String, AwesomeApiCotacao> resposta = Map.of("USDBRL",
                new AwesomeApiCotacao("USD", "BRL", "cinco e quarenta", "2026-09-08 10:31:02"));

        assertThatThrownBy(() -> adaptador.paraCotacao(Moeda.USD, resposta))
                .isInstanceOf(CotacaoIndisponivelException.class)
                .hasMessageContaining("formato inesperado");
    }
}
