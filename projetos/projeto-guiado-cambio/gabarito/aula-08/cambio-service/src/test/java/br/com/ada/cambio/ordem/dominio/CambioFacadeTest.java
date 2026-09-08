package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * A FACADE herdou os testes de orquestracao que eram do {@code OrdemService}.
 *
 * <p>Colaboradores puros (validador, calculadora, estrategias) entram REAIS;
 * so o que fala com o mundo (rede e banco) e mockado. Assim o teste exercita a
 * coreografia de verdade — inclusive a ordem: validar antes de gastar rede.</p>
 */
@ExtendWith(MockitoExtension.class)
class CambioFacadeTest {

    @Mock
    private ConsultaCliente consultaCliente;

    @Mock
    private ConsultaCotacao consultaCotacao;

    @Mock
    private OrdemService ordemService;

    private CambioFacade facade;

    @BeforeEach
    void montarFacade() {
        CalculadoraDeOperacao calculadora = new CalculadoraDeOperacao(Map.of(
                Moeda.USD, new CalculoUsdStrategy(),
                Moeda.EUR, new CalculoEurStrategy()));
        facade = new CambioFacade(new ValidadorDeOrdem(new CatalogoMoedasBean()),
                consultaCliente, consultaCotacao, calculadora, ordemService);
    }

    private NovaOrdem pedido(String cpf, String moeda, String valor, String agencia) {
        return new NovaOrdem(cpf, moeda, new BigDecimal(valor), agencia);
    }

    @Test
    @DisplayName("EUR: aplica o spread de 0,5% da estrategia do euro")
    void registraOrdemEmEuro() {
        when(consultaCliente.porCpf("43488428095"))
                .thenReturn(new ClienteEncontrado(1L, "43488428095", "Ana Souza"));
        when(consultaCotacao.vigente(Moeda.EUR))
                .thenReturn(new CotacaoVigente(Moeda.EUR, new BigDecimal("6.5857"), LocalDateTime.now()));
        when(ordemService.salvar(any(OrdemDeCompra.class))).thenAnswer(c -> c.getArgument(0));

        OrdemDeCompra ordem = facade.registrar(pedido("43488428095", "EUR", "100.00", "7057"));

        assertThat(ordem.getMoeda()).isEqualTo(Moeda.EUR);
        assertThat(ordem.getValorCotacao()).isEqualByComparingTo("6.5857");
        assertThat(ordem.getValorTotalOperacao()).isEqualByComparingTo("661.86");
    }

    @Test
    @DisplayName("USD: sem spread, o total e a conversao direta")
    void registraOrdemEmDolar() {
        when(consultaCliente.porCpf("43488428095"))
                .thenReturn(new ClienteEncontrado(1L, "43488428095", "Ana Souza"));
        when(consultaCotacao.vigente(Moeda.USD))
                .thenReturn(new CotacaoVigente(Moeda.USD, new BigDecimal("5.4321"), LocalDateTime.now()));
        when(ordemService.salvar(any(OrdemDeCompra.class))).thenAnswer(c -> c.getArgument(0));

        OrdemDeCompra ordem = facade.registrar(pedido("43488428095", "USD", "100.00", "7057"));

        assertThat(ordem.getValorTotalOperacao()).isEqualByComparingTo("543.21");
    }

    @Test
    @DisplayName("valida ANTES de gastar chamada de rede")
    void moedaNaoSuportadaNaoChamaVizinhos() {
        assertThatThrownBy(() -> facade.registrar(pedido("43488428095", "JPY", "100.00", "7057")))
                .isInstanceOf(MoedaNaoSuportadaException.class);

        verifyNoInteractions(consultaCliente, consultaCotacao);
        verify(ordemService, never()).salvar(any());
    }

    @Test
    @DisplayName("agencia invalida tambem para antes da rede")
    void agenciaInvalidaNaoChamaVizinhos() {
        assertThatThrownBy(() -> facade.registrar(pedido("43488428095", "USD", "100.00", "705")))
                .isInstanceOf(AgenciaInvalidaException.class);

        verifyNoInteractions(consultaCliente, consultaCotacao);
    }

    @Test
    @DisplayName("cliente inexistente aborta antes de consultar a cotacao")
    void clienteInexistente() {
        when(consultaCliente.porCpf("00000000000"))
                .thenThrow(new ClienteNaoEncontradoException("00000000000"));

        assertThatThrownBy(() -> facade.registrar(pedido("00000000000", "USD", "50.00", "7057")))
                .isInstanceOf(ClienteNaoEncontradoException.class);

        verifyNoInteractions(consultaCotacao);
        verify(ordemService, never()).salvar(any());
    }

    @Test
    @DisplayName("vizinho indisponivel sobe em linguagem de dominio")
    void vizinhoIndisponivel() {
        when(consultaCliente.porCpf("43488428095"))
                .thenThrow(new ServicoIndisponivelException("cliente-service", new RuntimeException()));

        assertThatThrownBy(() -> facade.registrar(pedido("43488428095", "USD", "50.00", "7057")))
                .isInstanceOf(ServicoIndisponivelException.class);
    }

    @Test
    @DisplayName("consulta por id delega ao OrdemService")
    void consultaPorId() {
        OrdemDeCompra gravada = new OrdemDeCompra(1L, "43488428095", LocalDateTime.now(), Moeda.USD,
                new BigDecimal("10.00"), new BigDecimal("5.4321"), new BigDecimal("54.32"), "7057");
        when(ordemService.buscarPorId(1L)).thenReturn(gravada);

        assertThat(facade.consultarPorId(1L).getCpfCliente()).isEqualTo("43488428095");
    }
}
