package br.com.ada.cambio.ordem.infra;

import br.com.ada.cambio.ordem.dominio.Moeda;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Detalhe de transporte: como se fala HTTP com o cotacao-service. */
@FeignClient(name = "cotacao-service", url = "${servicos.cotacao.url:}")
public interface CotacaoFeignClient {

    @GetMapping("/cotacoes/{moeda}")
    CotacaoResumoJson consultar(@PathVariable("moeda") Moeda moeda);
}
