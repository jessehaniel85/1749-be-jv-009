package br.com.ada.cambio.ordem.infra;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Um {@link RestClient} por servico chamado, cada um com a sua URL base e
 * timeout de 3s.
 *
 * <p>Sem timeout, o cambio-service fica preso esperando um vizinho lento e o
 * problema dele vira problema nosso.</p>
 */
@Configuration
public class ClientesHttpConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    @Bean
    public RestClient clienteRestClient(RestClient.Builder builder,
                                        @Value("${servicos.cliente.url}") String urlBase) {
        return construir(builder, urlBase);
    }

    @Bean
    public RestClient cotacaoRestClient(RestClient.Builder builder,
                                        @Value("${servicos.cotacao.url}") String urlBase) {
        return construir(builder, urlBase);
    }

    private RestClient construir(RestClient.Builder builder, String urlBase) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(TIMEOUT);
        fabrica.setReadTimeout(TIMEOUT);
        return builder.clone().baseUrl(urlBase).requestFactory(fabrica).build();
    }
}
