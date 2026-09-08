package br.com.ada.cambio.cotacao.api;

import br.com.ada.cambio.cotacao.dominio.CotacaoService;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import br.com.ada.cambio.cotacao.dominio.MoedaNaoSuportadaException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API HTTP do dominio Cotacao. */
@RestController
@RequestMapping("/cotacoes")
public class CotacaoController {

    private final CotacaoService cotacaoService;

    public CotacaoController(CotacaoService cotacaoService) {
        this.cotacaoService = cotacaoService;
    }

    @GetMapping("/{moeda}")
    public CotacaoResponse consultar(@PathVariable String moeda) {
        return CotacaoResponse.de(cotacaoService.consultar(resolver(moeda)));
    }

    @PutMapping("/{moeda}")
    public CotacaoResponse atualizar(@PathVariable String moeda,
                                     @Valid @RequestBody AtualizacaoCotacaoRequest requisicao) {
        return CotacaoResponse.de(cotacaoService.atualizar(resolver(moeda), requisicao.valorCotacao()));
    }

    private Moeda resolver(String sigla) {
        return Moeda.deSigla(sigla).orElseThrow(() -> new MoedaNaoSuportadaException(sigla));
    }
}
