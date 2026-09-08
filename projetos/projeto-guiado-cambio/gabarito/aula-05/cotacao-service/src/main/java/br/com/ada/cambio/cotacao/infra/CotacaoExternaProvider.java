package br.com.ada.cambio.cotacao.infra;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoIndisponivelException;
import br.com.ada.cambio.cotacao.dominio.CotacaoProvider;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Provedor ALTERNATIVO: consulta a awesomeapi.
 *
 * <p>Ativo apenas com {@code cotacao.provedor=externo} - fora da rede corporativa,
 * que bloqueia sites externos. Mesmo contrato do provedor local.</p>
 */
@Component
@ConditionalOnProperty(name = "cotacao.provedor", havingValue = "externo")
public class CotacaoExternaProvider implements CotacaoProvider {

    private final RestClient restClient;
    private final AwesomeApiAdapter adapter;

    public CotacaoExternaProvider(RestClient cotacaoExternaRestClient, AwesomeApiAdapter adapter) {
        this.restClient = cotacaoExternaRestClient;
        this.adapter = adapter;
    }

    @Override
    public Cotacao obter(Moeda moeda) {
        String chave = moeda.name() + "BRL";
        try {
            Map<String, AwesomeApiResposta> corpo = restClient.get()
                    .uri("/last/{moeda}-BRL", moeda.name())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (corpo == null || !corpo.containsKey(chave)) {
                throw new CotacaoIndisponivelException(
                        "awesomeapi nao devolveu a chave " + chave);
            }
            return adapter.paraCotacao(moeda, corpo.get(chave));
        } catch (RestClientException excecao) {
            throw new CotacaoIndisponivelException(
                    "Falha ao consultar a awesomeapi para " + moeda, excecao);
        }
    }
}
