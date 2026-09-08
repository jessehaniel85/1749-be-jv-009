package br.com.ada.cambio.ordem.infra;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Detalhe de transporte: como se fala HTTP com o cliente-service.
 *
 * <p>E o unico lugar do modulo que conhece a rota {@code /clientes/{cpf}}.
 * Nao e a porta do dominio — a porta e
 * {@link br.com.ada.cambio.ordem.dominio.ConsultaCliente}, e quem liga as duas
 * e o {@link ClienteClientAdapter}.</p>
 */
@FeignClient(name = "cliente-service", url = "${servicos.cliente.url:}")
public interface ClienteFeignClient {

    @GetMapping("/clientes/{cpf}")
    ClienteResumoJson buscarPorCpf(@PathVariable("cpf") String cpf);
}
