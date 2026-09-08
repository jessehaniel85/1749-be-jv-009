package br.com.ada.cambio.cotacao.infra;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoIndisponivelException;
import br.com.ada.cambio.cotacao.dominio.CotacaoProvider;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Provedor PADRAO: le a tabela local {@code cotacoes}, semeada por {@code data.sql}
 * e atualizavel por {@code PUT /cotacoes/{moeda}}.
 *
 * <p>Ativo quando {@code cotacao.provedor=local} ou quando a propriedade nao existe.</p>
 */
@Component
@ConditionalOnProperty(name = "cotacao.provedor", havingValue = "local", matchIfMissing = true)
public class CotacaoLocalProvider implements CotacaoProvider {

    private final CotacaoRepository repositorio;

    public CotacaoLocalProvider(CotacaoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Cotacao obter(Moeda moeda) {
        return repositorio.findByMoeda(moeda)
                .orElseThrow(() -> new CotacaoIndisponivelException(
                        "Nao ha cotacao local cadastrada para " + moeda
                                + ". Use PUT /cotacoes/" + moeda + " para informar um valor."));
    }
}
