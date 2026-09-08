package br.com.ada.cambio.cotacao.infra;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoIndisponivelException;
import br.com.ada.cambio.cotacao.dominio.CotacaoProvider;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Provedor alternativo: consulta a cotação na awesomeapi.
 *
 * <p>Ativo só com {@code cotacao.provedor=externo} — fora de redes que bloqueiam a internet.
 * Do ponto de vista do {@code CotacaoService}, é indistinguível do provedor local: mesmo
 * contrato, mesmo tipo de retorno. Trocar a origem do dado é mudar uma linha de YAML.</p>
 *
 * <p><b>Timeout explícito de 3 segundos</b> (conexão e leitura). Chamada remota sem timeout é
 * uma thread presa esperando para sempre: em produção, é assim que um provedor lento derruba
 * uma API inteira.</p>
 */
@Component
@ConditionalOnProperty(name = "cotacao.provedor", havingValue = "externo")
public class CotacaoExternaProvider implements CotacaoProvider {

    /** Tipo do corpo devolvido: um mapa chaveado por "USDBRL", "EURBRL"… */
    private static final ParameterizedTypeReference<Map<String, AwesomeApiCotacao>> TIPO_DA_RESPOSTA =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final AwesomeApiAdapter adaptador;

    public CotacaoExternaProvider(AwesomeApiAdapter adaptador,
                                  @Value("${cotacao.externo.url}") String urlBase,
                                  @Value("${cotacao.externo.timeout-segundos:3}") long timeoutEmSegundos) {
        this.adaptador = adaptador;
        this.restClient = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(fabricaComTimeout(Duration.ofSeconds(timeoutEmSegundos)))
                .build();
    }

    @Override
    public Cotacao obter(Moeda moeda) {
        try {
            Map<String, AwesomeApiCotacao> resposta = restClient.get()
                    .uri("/{moeda}-BRL", moeda.name())
                    .retrieve()
                    .body(TIPO_DA_RESPOSTA);

            // A tradução do modelo externo para o nosso NÃO acontece aqui: é papel do Adapter.
            return adaptador.paraCotacao(moeda, resposta);

        } catch (RestClientException erro) {
            throw new CotacaoIndisponivelException(moeda, "falha ao chamar o provedor externo: "
                    + erro.getMessage());
        }
    }

    private static SimpleClientHttpRequestFactory fabricaComTimeout(Duration timeout) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(timeout);
        fabrica.setReadTimeout(timeout);
        return fabrica;
    }
}
