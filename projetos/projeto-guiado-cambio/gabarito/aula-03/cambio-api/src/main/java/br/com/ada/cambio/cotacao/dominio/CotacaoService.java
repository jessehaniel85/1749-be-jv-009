package br.com.ada.cambio.cotacao.dominio;

import br.com.ada.cambio.cotacao.infra.CotacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta da cotação vigente.
 *
 * <p>Na Aula 3 a origem do dado é a tabela local, e o serviço fala com o repositório
 * direto. Na Aula 4 essa dependência vira um <b>contrato</b> ({@code CotacaoProvider}),
 * para que a mesma API funcione com cotação local ou com o provedor externo.</p>
 */
@Service
public class CotacaoService {

    private final CotacaoRepository repositorio;

    public CotacaoService(CotacaoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Cotação vigente da moeda.
     *
     * @throws MoedaNaoSuportadaException se não houver cotação registrada para a moeda
     */
    @Transactional(readOnly = true)
    public Cotacao consultar(Moeda moeda) {
        return repositorio.findByMoeda(moeda)
                .orElseThrow(() -> new MoedaNaoSuportadaException(String.valueOf(moeda)));
    }

    /** Mesma consulta, a partir da sigla recebida na URL. Sigla desconhecida → 422. */
    @Transactional(readOnly = true)
    public Cotacao consultarPorSigla(String sigla) {
        return consultar(Moeda.paraSigla(sigla));
    }
}
