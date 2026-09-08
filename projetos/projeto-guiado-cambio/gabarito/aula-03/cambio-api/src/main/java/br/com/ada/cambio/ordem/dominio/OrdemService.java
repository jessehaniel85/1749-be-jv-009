package br.com.ada.cambio.ordem.dominio;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.ClienteService;
import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoService;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import br.com.ada.cambio.ordem.infra.OrdemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro da ordem de compra: orquestra cliente + cotação + cálculo + persistência.
 *
 * <p><b>Isto ainda é um monólito.</b> As dependências de outros domínios entram por
 * injeção direta ({@code ClienteService}, {@code CotacaoService}) — uma chamada de método,
 * na mesma JVM e na mesma transação. Na Aula 5 essas duas linhas viram chamadas HTTP entre
 * serviços; o que muda de verdade é o custo do erro, não o desenho da orquestração.
 * Ver {@code docs/adr/ADR-001-monolito-por-enquanto.md}.</p>
 */
@Service
public class OrdemService {

    private static final Pattern AGENCIA_VALIDA =
            Pattern.compile("\\d{" + OrdemDeCompra.TAMANHO_AGENCIA + "}");

    private final OrdemRepository repositorio;
    private final ClienteService clienteService;
    private final CotacaoService cotacaoService;

    public OrdemService(OrdemRepository repositorio,
                        ClienteService clienteService,
                        CotacaoService cotacaoService) {
        this.repositorio = repositorio;
        this.clienteService = clienteService;
        this.cotacaoService = cotacaoService;
    }

    /**
     * Registra a ordem, calculando o total pela cotação vigente.
     *
     * @throws br.com.ada.cambio.cliente.dominio.ClienteNaoEncontradoException CPF sem cadastro (404)
     * @throws br.com.ada.cambio.cotacao.dominio.MoedaNaoSuportadaException moeda fora de USD/EUR (422)
     * @throws AgenciaInvalidaException agência sem 4 dígitos (422)
     */
    @Transactional
    public OrdemDeCompra registrar(String cpf,
                                   String siglaMoeda,
                                   BigDecimal valorMoedaEstrangeira,
                                   String numeroAgenciaRetirada) {
        Cliente cliente = clienteService.buscarPorCpf(cpf);
        validarAgencia(numeroAgenciaRetirada);

        Moeda moeda = Moeda.paraSigla(siglaMoeda);
        Cotacao cotacao = cotacaoService.consultar(moeda);
        BigDecimal total = calcularTotal(valorMoedaEstrangeira, cotacao.getValorCotacao());

        OrdemDeCompra ordem = new OrdemDeCompra(
                cliente.getId(),
                cliente.getCpf(),
                LocalDateTime.now(),
                moeda,
                valorMoedaEstrangeira,
                cotacao.getValorCotacao(),
                total,
                numeroAgenciaRetirada);

        return repositorio.save(ordem);
    }

    /**
     * Busca a ordem pelo id.
     *
     * @throws OrdemNaoEncontradaException se não existir (404)
     */
    @Transactional(readOnly = true)
    public OrdemDeCompra buscarPorId(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new OrdemNaoEncontradaException(id));
    }

    /**
     * {@code valorTotalOperacao = valorMoedaEstrangeira × valorCotacao}, arredondado a 2 casas
     * com {@code HALF_EVEN} (arredondamento bancário: empates vão para o par mais próximo,
     * o que não enviesa a soma de milhares de operações para cima).
     */
    private BigDecimal calcularTotal(BigDecimal valorMoedaEstrangeira, BigDecimal valorCotacao) {
        return valorMoedaEstrangeira.multiply(valorCotacao)
                .setScale(OrdemDeCompra.ESCALA_MONETARIA, RoundingMode.HALF_EVEN);
    }

    private void validarAgencia(String numeroAgenciaRetirada) {
        if (numeroAgenciaRetirada == null || !AGENCIA_VALIDA.matcher(numeroAgenciaRetirada).matches()) {
            throw new AgenciaInvalidaException(numeroAgenciaRetirada);
        }
    }
}
