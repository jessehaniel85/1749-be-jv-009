package br.com.ada.cambio.ordem.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FACADE — uma porta de entrada unica para a operacao de cambio.
 *
 * <p><b>Problema que resolve:</b> registrar uma ordem envolve cinco
 * colaboradores (validador, consulta de cliente, consulta de cotacao,
 * calculadora e persistencia) e uma ORDEM entre eles que importa: validar antes
 * de gastar chamada de rede, consultar cliente antes de cotacao. Se o controller
 * conhecesse os cinco, ele estaria carregando essa sequencia — e qualquer outro
 * ponto de entrada (uma fila, um job, um teste de aceitacao) teria de repeti-la.</p>
 *
 * <p><b>Depois do Facade</b> o controller depende de UMA coisa:</p>
 * <pre>
 *   antes:  OrdemController -> ValidadorDeOrdem, ConsultaCliente, ConsultaCotacao,
 *                              CalculadoraDeOperacao, OrdemService
 *   depois: OrdemController -> CambioFacade
 * </pre>
 *
 * <p>Facade <b>nao e</b> uma classe que "faz tudo": ela nao contem regra propria.
 * Toda decisao continua no colaborador certo — ela so conhece a coreografia.</p>
 */
@Service
public class CambioFacade {

    private final ValidadorDeOrdem validador;
    private final ConsultaCliente consultaCliente;
    private final ConsultaCotacao consultaCotacao;
    private final CalculadoraDeOperacao calculadora;
    private final OrdemService ordemService;

    public CambioFacade(ValidadorDeOrdem validador,
                        ConsultaCliente consultaCliente,
                        ConsultaCotacao consultaCotacao,
                        CalculadoraDeOperacao calculadora,
                        OrdemService ordemService) {
        this.validador = validador;
        this.consultaCliente = consultaCliente;
        this.consultaCotacao = consultaCotacao;
        this.calculadora = calculadora;
        this.ordemService = ordemService;
    }

    /**
     * Registra uma ordem de compra: valida, consulta cliente, consulta cotacao,
     * calcula pela estrategia da moeda e persiste.
     */
    @Transactional
    public OrdemDeCompra registrar(NovaOrdem pedido) {
        Moeda moeda = validador.moedaDe(pedido.siglaMoeda());
        validador.validarAgencia(pedido.numeroAgenciaRetirada());

        ClienteEncontrado cliente = consultaCliente.porCpf(pedido.cpfCliente());
        CotacaoVigente cotacao = consultaCotacao.vigente(moeda);

        return ordemService.salvar(montarOrdem(pedido, moeda, cliente, cotacao));
    }

    @Transactional(readOnly = true)
    public OrdemDeCompra consultarPorId(Long id) {
        return ordemService.buscarPorId(id);
    }

    private OrdemDeCompra montarOrdem(NovaOrdem pedido,
                                      Moeda moeda,
                                      ClienteEncontrado cliente,
                                      CotacaoVigente cotacao) {
        BigDecimal total = calculadora.calcularTotal(
                moeda, pedido.valorMoedaEstrangeira(), cotacao.valorCotacao());
        return new OrdemDeCompra(
                cliente.id(),
                cliente.cpf(),
                LocalDateTime.now(),
                moeda,
                pedido.valorMoedaEstrangeira(),
                cotacao.valorCotacao(),
                total,
                pedido.numeroAgenciaRetirada());
    }
}
