package br.com.ada.cambio.ordem.dominio;

import br.com.ada.cambio.ordem.infra.ClienteClient;
import br.com.ada.cambio.ordem.infra.ClienteResumo;
import br.com.ada.cambio.ordem.infra.CotacaoClient;
import br.com.ada.cambio.ordem.infra.CotacaoResumo;
import br.com.ada.cambio.ordem.infra.OrdemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra o registro de uma ordem de compra.
 *
 * <p>Faz validacao, consulta os dois servicos vizinhos, calcula o total e
 * persiste. Na Aula 7 essas responsabilidades sao separadas (SRP).</p>
 */
@Service
public class OrdemService {

    private final OrdemRepository repositorio;
    private final ClienteClient clienteClient;
    private final CotacaoClient cotacaoClient;

    public OrdemService(OrdemRepository repositorio, ClienteClient clienteClient, CotacaoClient cotacaoClient) {
        this.repositorio = repositorio;
        this.clienteClient = clienteClient;
        this.cotacaoClient = cotacaoClient;
    }

    @Transactional
    public OrdemDeCompra registrar(String cpfCliente,
                                   String siglaMoeda,
                                   BigDecimal valorMoedaEstrangeira,
                                   String numeroAgenciaRetirada) {

        Moeda moeda = Moeda.deSigla(siglaMoeda)
                .orElseThrow(() -> new MoedaNaoSuportadaException(siglaMoeda));

        if (numeroAgenciaRetirada == null || !numeroAgenciaRetirada.matches("\\d{4}")) {
            throw new AgenciaInvalidaException(numeroAgenciaRetirada);
        }

        ClienteResumo cliente = clienteClient.buscarPorCpf(cpfCliente);
        CotacaoResumo cotacao = cotacaoClient.consultar(moeda);

        BigDecimal valorTotalOperacao = valorMoedaEstrangeira
                .multiply(cotacao.valorCotacao())
                .setScale(2, RoundingMode.HALF_EVEN);

        OrdemDeCompra ordem = new OrdemDeCompra(
                cliente.id(),
                cliente.cpf(),
                LocalDateTime.now(),
                moeda,
                valorMoedaEstrangeira,
                cotacao.valorCotacao(),
                valorTotalOperacao,
                numeroAgenciaRetirada);

        return repositorio.save(ordem);
    }

    @Transactional(readOnly = true)
    public OrdemDeCompra buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new OrdemNaoEncontradaException(id));
    }
}
