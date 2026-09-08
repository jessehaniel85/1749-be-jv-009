package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.ordem.infra.ClienteClient;
import br.com.ada.cambio.ordem.infra.ClienteResumo;
import br.com.ada.cambio.ordem.infra.CotacaoClient;
import br.com.ada.cambio.ordem.infra.CotacaoResumo;
import br.com.ada.cambio.ordem.infra.OrdemRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * O teste-chave da Aula 5: os dois vizinhos viraram REDE, entao aqui eles
 * viram MOCK. Nenhuma porta e aberta durante este teste.
 */
@ExtendWith(MockitoExtension.class)
class OrdemServiceTest {

    @Mock
    private OrdemRepository repositorio;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private CotacaoClient cotacaoClient;

    @InjectMocks
    private OrdemService ordemService;

    @Test
    @DisplayName("registra a ordem calculando o total com HALF_EVEN em 2 casas")
    void registraOrdemCalculandoTotal() {
        when(clienteClient.buscarPorCpf("43488428095"))
                .thenReturn(new ClienteResumo(1L, "Ana Souza", "43488428095"));
        when(cotacaoClient.consultar(Moeda.EUR))
                .thenReturn(new CotacaoResumo("EUR", new BigDecimal("6.5857"), LocalDateTime.now()));
        when(repositorio.save(any(OrdemDeCompra.class))).thenAnswer(chamada -> chamada.getArgument(0));

        OrdemDeCompra ordem = ordemService.registrar(
                "43488428095", "EUR", new BigDecimal("100.00"), "7057");

        assertThat(ordem.getIdCliente()).isEqualTo(1L);
        assertThat(ordem.getMoeda()).isEqualTo(Moeda.EUR);
        assertThat(ordem.getValorCotacao()).isEqualByComparingTo("6.5857");
        assertThat(ordem.getValorTotalOperacao()).isEqualByComparingTo("658.57");
        assertThat(ordem.getValorTotalOperacao().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("moeda fora do catalogo nem chega a consultar os vizinhos")
    void moedaNaoSuportada() {
        assertThatThrownBy(() -> ordemService.registrar(
                "43488428095", "JPY", new BigDecimal("100.00"), "7057"))
                .isInstanceOf(MoedaNaoSuportadaException.class);

        verifyNoInteractions(clienteClient, cotacaoClient);
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("agencia fora do formato de 4 digitos e recusada")
    void agenciaInvalida() {
        assertThatThrownBy(() -> ordemService.registrar(
                "43488428095", "USD", new BigDecimal("100.00"), "705"))
                .isInstanceOf(AgenciaInvalidaException.class);

        verifyNoInteractions(clienteClient, cotacaoClient);
    }

    @Test
    @DisplayName("cliente inexistente no vizinho aborta a ordem")
    void clienteInexistente() {
        when(clienteClient.buscarPorCpf("00000000000"))
                .thenThrow(new ClienteNaoEncontradoException("00000000000"));

        assertThatThrownBy(() -> ordemService.registrar(
                "00000000000", "USD", new BigDecimal("50.00"), "7057"))
                .isInstanceOf(ClienteNaoEncontradoException.class);

        verifyNoInteractions(cotacaoClient);
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("busca por id devolve a ordem gravada")
    void buscaPorId() {
        OrdemDeCompra gravada = new OrdemDeCompra(1L, "43488428095", LocalDateTime.now(), Moeda.USD,
                new BigDecimal("10.00"), new BigDecimal("5.4321"), new BigDecimal("54.32"), "7057");
        when(repositorio.findById(1L)).thenReturn(Optional.of(gravada));

        assertThat(ordemService.buscarPorId(1L).getCpfCliente()).isEqualTo("43488428095");
    }

    @Test
    @DisplayName("id inexistente estoura OrdemNaoEncontradaException")
    void ordemInexistente() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordemService.buscarPorId(99L))
                .isInstanceOf(OrdemNaoEncontradaException.class);
    }
}
