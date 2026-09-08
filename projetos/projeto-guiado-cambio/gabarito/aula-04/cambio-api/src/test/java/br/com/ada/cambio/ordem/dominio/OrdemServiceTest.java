package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.cliente.dominio.ClienteService;
import br.com.ada.cambio.cliente.dominio.EstadoCivil;
import br.com.ada.cambio.cliente.dominio.Sexo;
import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoService;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import br.com.ada.cambio.cotacao.dominio.MoedaNaoSuportadaException;
import br.com.ada.cambio.ordem.infra.OrdemRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unitário do coração da regra: o cálculo do total e as três recusas (cliente, agência, moeda).
 *
 * <p>Os dois colaboradores de outros domínios são mocks. Isso deixa explícito que
 * {@code OrdemService} <b>orquestra</b>, e que o teste dele não precisa nem de banco nem
 * de HTTP para valer.</p>
 */
@ExtendWith(MockitoExtension.class)
class OrdemServiceTest {

    @Mock
    private OrdemRepository repositorio;

    @Mock
    private ClienteService clienteService;

    @Mock
    private CotacaoService cotacaoService;

    @InjectMocks
    private OrdemService servico;

    private Cliente clienteCadastrado() {
        Cliente cliente = new Cliente("Marina Alcântara", "43488428095",
                LocalDate.of(1991, 4, 17), EstadoCivil.SOLTEIRO, Sexo.FEMININO);
        ReflectionTestUtils.setField(cliente, "id", 1L);
        return cliente;
    }

    private void comCotacao(Moeda moeda, String valor) {
        when(cotacaoService.consultar(moeda))
                .thenReturn(new Cotacao(moeda, new BigDecimal(valor), LocalDateTime.now()));
    }

    private void salvaDevolvendoAMesmaOrdem() {
        when(repositorio.save(any(OrdemDeCompra.class))).thenAnswer(c -> c.getArgument(0));
    }

    @Test
    @DisplayName("registra a ordem calculando o total pela cotação vigente")
    void registraCalculandoOTotal() {
        when(clienteService.buscarPorCpf("43488428095")).thenReturn(clienteCadastrado());
        comCotacao(Moeda.EUR, "6.5857");
        salvaDevolvendoAMesmaOrdem();

        OrdemDeCompra ordem = servico.registrar("43488428095", "EUR", new BigDecimal("100.0"), "7057");

        assertThat(ordem.getIdCliente()).isEqualTo(1L);
        assertThat(ordem.getCpfCliente()).isEqualTo("43488428095");
        assertThat(ordem.getMoeda()).isEqualTo(Moeda.EUR);
        assertThat(ordem.getValorCotacao()).isEqualByComparingTo("6.5857");
        assertThat(ordem.getValorTotalOperacao()).isEqualByComparingTo("658.57");
        assertThat(ordem.getValorTotalOperacao().scale()).isEqualTo(2);
        assertThat(ordem.getNumeroAgenciaRetirada()).isEqualTo("7057");
        assertThat(ordem.getDataSolicitacao()).isNotNull();
    }

    @Test
    @DisplayName("arredonda o total com HALF_EVEN (o empate vai para o dígito par)")
    void arredondaComHalfEven() {
        when(clienteService.buscarPorCpf("43488428095")).thenReturn(clienteCadastrado());
        comCotacao(Moeda.USD, "5.4350");
        salvaDevolvendoAMesmaOrdem();

        OrdemDeCompra ordem = servico.registrar("43488428095", "USD", BigDecimal.ONE, "7057");

        // 5.4350 está exatamente no meio: HALF_UP daria 5.44 sempre; HALF_EVEN vai para o par → 5.44
        // (3 é ímpar, sobe). Com 6.5850 o mesmo critério desceria para 6.58.
        assertThat(ordem.getValorTotalOperacao()).isEqualByComparingTo("5.44");
    }

    @Test
    @DisplayName("cliente inexistente propaga o 404 e nada é salvo")
    void clienteInexistenteNaoRegistra() {
        when(clienteService.buscarPorCpf("00000000000"))
                .thenThrow(new ClienteNaoEncontradoException("00000000000"));

        assertThatThrownBy(() -> servico.registrar("00000000000", "USD", BigDecimal.TEN, "7057"))
                .isInstanceOf(ClienteNaoEncontradoException.class);

        verifyNoInteractions(cotacaoService);
        verify(repositorio, never()).save(any(OrdemDeCompra.class));
    }

    @Test
    @DisplayName("agência fora do formato de 4 dígitos é recusada antes de consultar a cotação")
    void agenciaInvalidaNaoRegistra() {
        when(clienteService.buscarPorCpf("43488428095")).thenReturn(clienteCadastrado());

        assertThatThrownBy(() -> servico.registrar("43488428095", "USD", BigDecimal.TEN, "705"))
                .isInstanceOf(AgenciaInvalidaException.class)
                .hasMessageContaining("705");

        verifyNoInteractions(cotacaoService);
        verify(repositorio, never()).save(any(OrdemDeCompra.class));
    }

    @Test
    @DisplayName("moeda fora de USD/EUR é recusada com MoedaNaoSuportadaException")
    void moedaNaoSuportadaNaoRegistra() {
        when(clienteService.buscarPorCpf("43488428095")).thenReturn(clienteCadastrado());

        assertThatThrownBy(() -> servico.registrar("43488428095", "JPY", BigDecimal.TEN, "7057"))
                .isInstanceOf(MoedaNaoSuportadaException.class)
                .hasMessageContaining("JPY");

        verify(repositorio, never()).save(any(OrdemDeCompra.class));
    }

    @Test
    @DisplayName("buscar ordem inexistente lança OrdemNaoEncontradaException")
    void buscaOrdemInexistente() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorId(99L))
                .isInstanceOf(OrdemNaoEncontradaException.class)
                .hasMessageContaining("99");
    }
}
