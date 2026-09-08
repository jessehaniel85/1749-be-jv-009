package br.com.ada.cambio.cotacao.dominio;

import br.com.ada.cambio.cotacao.infra.CotacaoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta e atualização da cotação vigente.
 *
 * <p>Desde a Aula 4 a leitura passa pelo contrato {@link CotacaoProvider} — o serviço não
 * sabe mais de onde vem o número. A escrita ({@code PUT /cotacoes/{moeda}}) continua indo
 * direto ao repositório, porque ela só faz sentido para o <b>provedor local</b>: é o
 * mecanismo de simular variação de câmbio em sala, sem depender de rede externa.</p>
 */
@Service
public class CotacaoService {

    private final CotacaoProvider provedor;
    private final CotacaoRepository repositorio;

    public CotacaoService(CotacaoProvider provedor, CotacaoRepository repositorio) {
        this.provedor = provedor;
        this.repositorio = repositorio;
    }

    /**
     * Cotação vigente da moeda, seja qual for o provedor configurado.
     *
     * @throws MoedaNaoSuportadaException se a moeda não é operada
     */
    @Transactional(readOnly = true)
    public Cotacao consultar(Moeda moeda) {
        return provedor.obter(moeda);
    }

    /** Mesma consulta, a partir da sigla recebida na URL. Sigla desconhecida → 422. */
    @Transactional(readOnly = true)
    public Cotacao consultarPorSigla(String sigla) {
        return consultar(Moeda.paraSigla(sigla));
    }

    /**
     * Atualiza a cotação da tabela local (simulação de variação de mercado).
     *
     * <p>Só afeta o provedor {@code local}. Com {@code cotacao.provedor=externo}, a alteração
     * é gravada mas a consulta continua vindo da awesomeapi.</p>
     *
     * @throws MoedaNaoSuportadaException se a moeda não é operada
     */
    @Transactional
    public Cotacao atualizar(String sigla, BigDecimal novoValor) {
        Moeda moeda = Moeda.paraSigla(sigla);
        Cotacao cotacao = repositorio.findByMoeda(moeda)
                .orElseThrow(() -> new MoedaNaoSuportadaException(sigla));
        cotacao.atualizar(novoValor.setScale(Cotacao.ESCALA_COTACAO, RoundingMode.HALF_EVEN),
                LocalDateTime.now());
        return repositorio.save(cotacao);
    }
}
