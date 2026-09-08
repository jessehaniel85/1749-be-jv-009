package br.com.ada.cambio.cotacao.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoIndisponivelException;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CotacaoLocalProviderTest {

    @Mock
    private CotacaoRepository repositorio;

    @InjectMocks
    private CotacaoLocalProvider provider;

    @Test
    @DisplayName("le a cotacao da tabela local")
    void leDaTabelaLocal() {
        when(repositorio.findByMoeda(Moeda.EUR))
                .thenReturn(Optional.of(new Cotacao(Moeda.EUR, new BigDecimal("6.5857"), LocalDateTime.now())));

        assertThat(provider.obter(Moeda.EUR).getValorCotacao()).isEqualByComparingTo("6.5857");
    }

    @Test
    @DisplayName("sem linha na tabela, avisa que a cotacao esta indisponivel")
    void semLinhaLocal() {
        when(repositorio.findByMoeda(Moeda.USD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider.obter(Moeda.USD))
                .isInstanceOf(CotacaoIndisponivelException.class);
    }
}
