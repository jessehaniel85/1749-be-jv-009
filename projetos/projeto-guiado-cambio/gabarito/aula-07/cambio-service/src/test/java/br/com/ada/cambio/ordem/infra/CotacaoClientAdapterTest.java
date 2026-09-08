package br.com.ada.cambio.ordem.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.ordem.FeignErros;
import br.com.ada.cambio.ordem.dominio.CotacaoVigente;
import br.com.ada.cambio.ordem.dominio.Moeda;
import br.com.ada.cambio.ordem.dominio.MoedaNaoSuportadaException;
import br.com.ada.cambio.ordem.dominio.ServicoIndisponivelException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CotacaoClientAdapterTest {

    @Mock
    private CotacaoFeignClient feignClient;

    @InjectMocks
    private CotacaoClientAdapter adapter;

    @Test
    @DisplayName("traduz o JSON do vizinho para o modelo do dominio")
    void traduzOJson() {
        when(feignClient.consultar(Moeda.EUR)).thenReturn(
                new CotacaoResumoJson("EUR", new BigDecimal("6.5857"), LocalDateTime.now()));

        CotacaoVigente cotacao = adapter.vigente(Moeda.EUR);

        assertThat(cotacao.moeda()).isEqualTo(Moeda.EUR);
        assertThat(cotacao.valorCotacao()).isEqualByComparingTo("6.5857");
    }

    @Test
    @DisplayName("422 do vizinho vira MoedaNaoSuportadaException")
    void traduzRecusa() {
        when(feignClient.consultar(Moeda.USD))
                .thenThrow(FeignErros.naoProcessavel("/cotacoes/USD"));

        assertThatThrownBy(() -> adapter.vigente(Moeda.USD))
                .isInstanceOf(MoedaNaoSuportadaException.class);
    }

    @Test
    @DisplayName("vizinho sem atender vira ServicoIndisponivelException")
    void traduzForaDoAr() {
        when(feignClient.consultar(Moeda.USD))
                .thenThrow(FeignErros.foraDoAr("/cotacoes/USD"));

        assertThatThrownBy(() -> adapter.vigente(Moeda.USD))
                .isInstanceOf(ServicoIndisponivelException.class)
                .hasMessageContaining("cotacao-service");
    }
}
