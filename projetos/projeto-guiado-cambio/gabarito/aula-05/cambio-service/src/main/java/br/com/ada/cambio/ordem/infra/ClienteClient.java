package br.com.ada.cambio.ordem.infra;

import br.com.ada.cambio.ordem.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.ordem.dominio.ServicoIndisponivelException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Porta de saida para o cliente-service.
 *
 * <p>Na Aula 5 a URL e FIXA ({@code servicos.cliente.url}). Na Aula 6 isso vira
 * um nome logico resolvido pelo Eureka - o codigo de negocio nao muda.</p>
 *
 * <p>O papel desta classe e traduzir <b>problema de transporte</b> em
 * <b>excecao de dominio</b>: quem chama nao precisa saber o que e um 404 HTTP.</p>
 */
@Component
public class ClienteClient {

    private static final String NOME_DO_SERVICO = "cliente-service (8081)";

    private final RestClient restClient;

    public ClienteClient(RestClient clienteRestClient) {
        this.restClient = clienteRestClient;
    }

    /**
     * Busca o cliente pelo CPF.
     *
     * @throws ClienteNaoEncontradoException se o cliente-service responder 404
     * @throws ServicoIndisponivelException se o cliente-service nao responder ou falhar (5xx)
     */
    public ClienteResumo buscarPorCpf(String cpf) {
        try {
            return restClient.get()
                    .uri("/clientes/{cpf}", cpf)
                    .retrieve()
                    .body(ClienteResumo.class);
        } catch (HttpClientErrorException.NotFound excecao) {
            throw new ClienteNaoEncontradoException(cpf);
        } catch (ResourceAccessException | HttpServerErrorException excecao) {
            throw new ServicoIndisponivelException(NOME_DO_SERVICO, excecao);
        }
    }
}
