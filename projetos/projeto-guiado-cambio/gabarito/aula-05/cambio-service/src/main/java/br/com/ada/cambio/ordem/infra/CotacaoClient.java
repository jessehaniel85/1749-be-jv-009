package br.com.ada.cambio.ordem.infra;

import br.com.ada.cambio.ordem.dominio.Moeda;
import br.com.ada.cambio.ordem.dominio.MoedaNaoSuportadaException;
import br.com.ada.cambio.ordem.dominio.ServicoIndisponivelException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/** Porta de saida para o cotacao-service. */
@Component
public class CotacaoClient {

    private static final String NOME_DO_SERVICO = "cotacao-service (8082)";

    private final RestClient restClient;

    public CotacaoClient(RestClient cotacaoRestClient) {
        this.restClient = cotacaoRestClient;
    }

    /**
     * Consulta a cotacao vigente.
     *
     * @throws MoedaNaoSuportadaException se o cotacao-service responder 422
     * @throws ServicoIndisponivelException se o cotacao-service nao responder ou falhar (5xx)
     */
    public CotacaoResumo consultar(Moeda moeda) {
        try {
            return restClient.get()
                    .uri("/cotacoes/{moeda}", moeda.name())
                    .retrieve()
                    .body(CotacaoResumo.class);
        } catch (HttpClientErrorException.UnprocessableEntity excecao) {
            throw new MoedaNaoSuportadaException(moeda.name());
        } catch (ResourceAccessException | HttpServerErrorException excecao) {
            throw new ServicoIndisponivelException(NOME_DO_SERVICO, excecao);
        }
    }
}
