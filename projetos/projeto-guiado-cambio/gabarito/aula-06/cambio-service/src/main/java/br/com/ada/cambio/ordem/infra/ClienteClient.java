package br.com.ada.cambio.ordem.infra;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente HTTP DECLARATIVO do cliente-service.
 *
 * <p>Compare com a Aula 5: sumiram o {@code RestClient}, o {@code baseUrl}, o
 * {@code try/catch} e a montagem da URI. Sobrou o contrato. A implementacao e
 * gerada em tempo de execucao pelo OpenFeign.</p>
 *
 * <p><b>{@code name}</b> e o nome logico registrado no Eureka - o mesmo
 * {@code spring.application.name} do outro servico. Nao e host, nao e porta.</p>
 *
 * <p><b>{@code url}</b> usa a sintaxe {@code ${prop:}} (valor padrao VAZIO):</p>
 * <ul>
 *   <li>perfil padrao - a propriedade nao existe, {@code url} fica vazia e o
 *       Feign resolve o nome pelo Eureka + LoadBalancer;</li>
 *   <li>perfil {@code plano-c} - a propriedade existe, o Feign vai direto na
 *       URL fixa e o Eureka nem entra na jogada.</li>
 * </ul>
 * A mesma anotacao serve aos dois planos.
 */
@FeignClient(name = "cliente-service", url = "${servicos.cliente.url:}")
public interface ClienteClient {

    @GetMapping("/clientes/{cpf}")
    ClienteResumo buscarPorCpf(@PathVariable("cpf") String cpf);
}
