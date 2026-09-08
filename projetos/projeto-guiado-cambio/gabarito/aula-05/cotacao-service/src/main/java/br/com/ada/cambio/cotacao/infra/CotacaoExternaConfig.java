package br.com.ada.cambio.cotacao.infra;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Monta o {@link RestClient} da awesomeapi com timeout curto (3s).
 *
 * <p>Timeout e obrigatorio em chamada de rede: sem ele, um provedor lento
 * congela a thread do nosso servico.</p>
 */
@Configuration
@ConditionalOnProperty(name = "cotacao.provedor", havingValue = "externo")
public class CotacaoExternaConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    @Bean
    public RestClient cotacaoExternaRestClient(RestClient.Builder builder,
                                               @Value("${cotacao.externo.url}") String urlBase) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(TIMEOUT);
        fabrica.setReadTimeout(TIMEOUT);
        return builder.baseUrl(urlBase).requestFactory(fabrica).build();
    }
}
