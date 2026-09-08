package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.ordem.infra.OrdemRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Mesmas asserçoes da Aula 6 — o comportamento nao mudou. O que mudou foram os
 * colaboradores: agora sao {@link ConsultaCliente} e {@link ConsultaCotacao},
 * abstracoes do proprio dominio. <b>O teste nao importa nem Feign nem HTTP.</b>
 *
 * <p>{@link ValidadorDeOrdem} e {@link CalculadoraDeOperacao} entram REAIS: sao
 * puros e rapidos, e mocka-los so esconderia a regra que queremos exercitar.</p>
 */
@ExtendWith(MockitoExtension.class)
class OrdemServiceTest {

    @Mock
    private OrdemRepository repositorio;

    @Mock
    private ConsultaCliente consultaCliente;

    @Mock
    private ConsultaCotacao consultaCotacao;

    private OrdemService ordemService;

    @BeforeEach
    void montarServico() {
        ordemService = new OrdemService(repositorio, consultaCliente, consultaCotacao,
                new ValidadorDeOrdem(), new CalculadoraDeOperacao());
    }

    private NovaOrdem pedido(String cpf, String moeda, String valor, String agencia) {
        return new NovaOrdem(cpf, moeda, new BigDecimal(valor), agencia);
    }

    @Test
    @DisplayName("registra a ordem calculando o total com HALF_EVEN em 2 casas")
    void registraOrdemCalculandoTotal() {
        when(consultaCliente.porCpf("43488428095"))
                .thenReturn(new ClienteEncontrado(1L, "43488428095", "Ana Souza"));
        when(consultaCotacao.vigente(Moeda.EUR))
                .thenReturn(new CotacaoVigente(Moeda.EUR, new BigDecimal("6.5857"), LocalDateTime.now()));
        when(repositorio.save(any(OrdemDeCompra.class))).thenAnswer(chamada -> chamada.getArgument(0));

        OrdemDeCompra ordem = ordemService.registrar(
                pedido("43488428095", "EUR", "100.00", "7057"));

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
                pedido("43488428095", "JPY", "100.00", "7057")))
                .isInstanceOf(MoedaNaoSuportadaException.class);

        verifyNoInteractions(consultaCliente, consultaCotacao);
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("agencia fora do formato de 4 digitos e recusada")
    void agenciaInvalida() {
        assertThatThrownBy(() -> ordemService.registrar(
                pedido("43488428095", "USD", "100.00", "705")))
                .isInstanceOf(AgenciaInvalidaException.class);

        verifyNoInteractions(consultaCliente, consultaCotacao);
    }

    @Test
    @DisplayName("cliente inexistente aborta a ordem — em linguagem de dominio")
    void clienteInexistente() {
        when(consultaCliente.porCpf("00000000000"))
                .thenThrow(new ClienteNaoEncontradoException("00000000000"));

        assertThatThrownBy(() -> ordemService.registrar(
                pedido("00000000000", "USD", "50.00", "7057")))
                .isInstanceOf(ClienteNaoEncontradoException.class);

        verifyNoInteractions(consultaCotacao);
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("vizinho fora do ar aborta a ordem — em linguagem de dominio")
    void vizinhoIndisponivel() {
        when(consultaCliente.porCpf("43488428095"))
                .thenThrow(new ServicoIndisponivelException("cliente-service", new RuntimeException()));

        assertThatThrownBy(() -> ordemService.registrar(
                pedido("43488428095", "USD", "50.00", "7057")))
                .isInstanceOf(ServicoIndisponivelException.class);
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
