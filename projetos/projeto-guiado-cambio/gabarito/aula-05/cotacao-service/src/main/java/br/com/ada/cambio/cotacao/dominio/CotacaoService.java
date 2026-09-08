package br.com.ada.cambio.cotacao.dominio;

import br.com.ada.cambio.cotacao.infra.CotacaoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta e atualizacao de cotacoes. */
@Service
public class CotacaoService {

    private final CotacaoProvider provider;
    private final CotacaoRepository repositorio;

    public CotacaoService(CotacaoProvider provider, CotacaoRepository repositorio) {
        this.provider = provider;
        this.repositorio = repositorio;
    }

    /** Consulta a cotacao vigente pelo provedor configurado. */
    @Transactional(readOnly = true)
    public Cotacao consultar(Moeda moeda) {
        return provider.obter(moeda);
    }

    /**
     * Atualiza (ou cria) a cotacao local da moeda. Serve para simular variacao
     * em aula sem depender de rede.
     */
    @Transactional
    public Cotacao atualizar(Moeda moeda, BigDecimal novoValor) {
        LocalDateTime agora = LocalDateTime.now();
        return repositorio.findByMoeda(moeda)
                .map(cotacao -> {
                    cotacao.atualizar(novoValor, agora);
                    return cotacao;
                })
                .orElseGet(() -> repositorio.save(new Cotacao(moeda, novoValor, agora)));
    }
}
