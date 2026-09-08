package br.com.ada.cambio.ordem.infra;

import br.com.ada.cambio.ordem.dominio.Moeda;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Cliente HTTP declarativo do cotacao-service. */
@FeignClient(name = "cotacao-service", url = "${servicos.cotacao.url:}")
public interface CotacaoClient {

    @GetMapping("/cotacoes/{moeda}")
    CotacaoResumo consultar(@PathVariable("moeda") Moeda moeda);
}
