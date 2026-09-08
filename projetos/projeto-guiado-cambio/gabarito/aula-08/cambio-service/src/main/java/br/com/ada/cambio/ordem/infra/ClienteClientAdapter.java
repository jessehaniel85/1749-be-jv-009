package br.com.ada.cambio.ordem.infra;

import br.com.ada.cambio.ordem.dominio.ClienteEncontrado;
import br.com.ada.cambio.ordem.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.ordem.dominio.ConsultaCliente;
import br.com.ada.cambio.ordem.dominio.ServicoIndisponivelException;
import feign.FeignException;
import org.springframework.stereotype.Component;

/**
 * ADAPTER + DIP: liga a porta do dominio ({@link ConsultaCliente}) ao detalhe de
 * transporte ({@link ClienteFeignClient}).
 *
 * <p>Duas traducoes acontecem aqui, e so aqui:</p>
 * <ol>
 *   <li><b>de dado:</b> {@link ClienteResumoJson} (formato do outro) vira
 *       {@link ClienteEncontrado} (formato nosso);</li>
 *   <li><b>de erro:</b> {@code FeignException.NotFound} vira
 *       {@link ClienteNaoEncontradoException}; qualquer outra falha de rede vira
 *       {@link ServicoIndisponivelException}.</li>
 * </ol>
 *
 * <p>Este arquivo e o unico do cambio-service que importa {@code feign}. Compare
 * com a Aula 6, onde ate o {@code @RestControllerAdvice} importava.</p>
 */
@Component
public class ClienteClientAdapter implements ConsultaCliente {

    private static final String NOME_DO_SERVICO = "cliente-service";

    private final ClienteFeignClient feignClient;

    public ClienteClientAdapter(ClienteFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public ClienteEncontrado porCpf(String cpf) {
        try {
            return converter(feignClient.buscarPorCpf(cpf));
        } catch (FeignException.NotFound excecao) {
            throw new ClienteNaoEncontradoException(cpf);
        } catch (FeignException | IllegalStateException excecao) {
            // IllegalStateException = "No instances available": o LoadBalancer
            // nao achou ninguem registrado no Eureka com esse nome.
            throw new ServicoIndisponivelException(NOME_DO_SERVICO, excecao);
        }
    }

    private ClienteEncontrado converter(ClienteResumoJson json) {
        return new ClienteEncontrado(json.id(), json.cpf(), json.nome());
    }
}
