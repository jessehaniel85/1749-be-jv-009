package br.com.ada.cambio.cotacao.infra;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoProvider;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import br.com.ada.cambio.cotacao.dominio.MoedaNaoSuportadaException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Provedor padrão: lê a tabela local {@code cotacoes} (semeada por {@code data.sql}).
 *
 * <p>É o provedor da sala de aula — não depende de rede externa, e o
 * {@code PUT /cotacoes/{moeda}} permite simular a variação do câmbio ao vivo.</p>
 *
 * <p>{@code matchIfMissing = true}: sem a propriedade configurada, este é o provedor ativo.
 * O piso seguro é sempre o que não precisa de internet.</p>
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
                .orElseThrow(() -> new MoedaNaoSuportadaException(String.valueOf(moeda)));
    }
}
