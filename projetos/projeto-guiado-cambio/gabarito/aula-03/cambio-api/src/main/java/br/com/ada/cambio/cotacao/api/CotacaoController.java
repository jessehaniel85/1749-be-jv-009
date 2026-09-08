package br.com.ada.cambio.cotacao.api;

import br.com.ada.cambio.cotacao.dominio.CotacaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Borda HTTP do domínio Cotação. */
@RestController
@RequestMapping("/cotacoes")
public class CotacaoController {

    private final CotacaoService servico;

    public CotacaoController(CotacaoService servico) {
        this.servico = servico;
    }

    /**
     * {@code GET /cotacoes/{moeda}} → 200 com a cotação vigente, ou 422 se a sigla não for
     * operada. O parâmetro é {@code String} (e não {@code Moeda}) para que a sigla inválida
     * chegue ao domínio e vire 422 — se fosse {@code Moeda}, o Spring devolveria 400 antes.
     */
    @GetMapping("/{moeda}")
    public CotacaoResponse consultar(@PathVariable String moeda) {
        return CotacaoResponse.de(servico.consultarPorSigla(moeda));
    }
}
