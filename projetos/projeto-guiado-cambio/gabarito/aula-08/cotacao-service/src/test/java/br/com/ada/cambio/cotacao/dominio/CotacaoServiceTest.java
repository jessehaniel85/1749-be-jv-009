package br.com.ada.cambio.cotacao.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.cotacao.infra.CotacaoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unitario: o service so orquestra provider e repositorio. */
@ExtendWith(MockitoExtension.class)
class CotacaoServiceTest {

    @Mock
    private CotacaoProvider provider;

    @Mock
    private CotacaoRepository repositorio;

    @InjectMocks
    private CotacaoService cotacaoService;

    @Test
    @DisplayName("consulta delega ao provedor configurado")
    void consultaDelegaAoProvider() {
        Cotacao esperada = new Cotacao(Moeda.USD, new BigDecimal("5.4321"), LocalDateTime.now());
        when(provider.obter(Moeda.USD)).thenReturn(esperada);

        Cotacao obtida = cotacaoService.consultar(Moeda.USD);

        assertThat(obtida.getValorCotacao()).isEqualByComparingTo("5.4321");
        verify(provider).obter(Moeda.USD);
    }

    @Test
    @DisplayName("atualizacao altera a linha existente sem criar outra")
    void atualizaCotacaoExistente() {
        Cotacao existente = new Cotacao(Moeda.EUR, new BigDecimal("6.5857"), LocalDateTime.now());
        when(repositorio.findByMoeda(Moeda.EUR)).thenReturn(Optional.of(existente));

        Cotacao atualizada = cotacaoService.atualizar(Moeda.EUR, new BigDecimal("7.0000"));

        assertThat(atualizada.getValorCotacao()).isEqualByComparingTo("7.0000");
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("atualizacao cria a linha quando a moeda ainda nao tem cotacao")
    void criaCotacaoInexistente() {
        when(repositorio.findByMoeda(Moeda.USD)).thenReturn(Optional.empty());
        when(repositorio.save(any(Cotacao.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Cotacao criada = cotacaoService.atualizar(Moeda.USD, new BigDecimal("5.9999"));

        assertThat(criada.getMoeda()).isEqualTo(Moeda.USD);
        verify(repositorio).save(any(Cotacao.class));
    }
}
