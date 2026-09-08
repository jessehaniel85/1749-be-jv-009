package br.com.ada.cambio.ordem.dominio;

import br.com.ada.cambio.ordem.infra.OrdemRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SRP: uma unica razao para mudar — <b>o passo a passo do registro de uma
 * ordem</b>. Validar e calcular sao de outros.
 *
 * <p>DIP: os campos sao {@link ConsultaCliente} e {@link ConsultaCotacao},
 * interfaces do proprio pacote {@code dominio}. Esta classe NAO importa
 * {@code feign}, NAO importa {@code infra} (fora do repositorio) e nao sabe
 * sequer que existe HTTP no meio do caminho. Trocar Feign por gRPC amanha nao
 * encosta neste arquivo.</p>
 */
@Service
public class OrdemService {

    private final OrdemRepository repositorio;
    private final ConsultaCliente consultaCliente;
    private final ConsultaCotacao consultaCotacao;
    private final ValidadorDeOrdem validador;
    private final CalculadoraDeOperacao calculadora;

    public OrdemService(OrdemRepository repositorio,
                        ConsultaCliente consultaCliente,
                        ConsultaCotacao consultaCotacao,
                        ValidadorDeOrdem validador,
                        CalculadoraDeOperacao calculadora) {
        this.repositorio = repositorio;
        this.consultaCliente = consultaCliente;
        this.consultaCotacao = consultaCotacao;
        this.validador = validador;
        this.calculadora = calculadora;
    }

    /**
     * Registra uma ordem de compra.
     *
     * <p>Cinco linhas, um nivel de abstracao: cada uma delega a quem sabe fazer.
     * Le-se como o enunciado da historia US-05.</p>
     */
    @Transactional
    public OrdemDeCompra registrar(NovaOrdem pedido) {
        Moeda moeda = validador.moedaDe(pedido.siglaMoeda());
        validador.validarAgencia(pedido.numeroAgenciaRetirada());

        ClienteEncontrado cliente = consultaCliente.porCpf(pedido.cpfCliente());
        CotacaoVigente cotacao = consultaCotacao.vigente(moeda);

        return repositorio.save(montarOrdem(pedido, moeda, cliente, cotacao));
    }

    @Transactional(readOnly = true)
    public OrdemDeCompra buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new OrdemNaoEncontradaException(id));
    }

    private OrdemDeCompra montarOrdem(NovaOrdem pedido,
                                      Moeda moeda,
                                      ClienteEncontrado cliente,
                                      CotacaoVigente cotacao) {
        BigDecimal total = calculadora.calcularTotal(
                pedido.valorMoedaEstrangeira(), cotacao.valorCotacao());
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
