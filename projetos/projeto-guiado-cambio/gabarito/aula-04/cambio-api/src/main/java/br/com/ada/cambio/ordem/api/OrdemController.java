package br.com.ada.cambio.ordem.api;

import br.com.ada.cambio.ordem.dominio.OrdemDeCompra;
import br.com.ada.cambio.ordem.dominio.OrdemService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** Borda HTTP do domínio Ordem de compra. */
@RestController
@RequestMapping("/ordens")
public class OrdemController {

    private final OrdemService servico;

    public OrdemController(OrdemService servico) {
        this.servico = servico;
    }

    /** {@code POST /ordens} → 201 com o comprovante · 404 cliente · 422 moeda ou agência. */
    @PostMapping
    public ResponseEntity<OrdemResponse> registrar(@RequestBody @Valid OrdemRequest requisicao,
                                                   UriComponentsBuilder construtorDeUri) {
        OrdemDeCompra ordem = servico.registrar(
                requisicao.cpf(),
                requisicao.moeda(),
                requisicao.valorMoedaEstrangeira(),
                requisicao.numeroAgenciaRetirada());

        URI localizacao = construtorDeUri.path("/ordens/{id}").buildAndExpand(ordem.getId()).toUri();
        return ResponseEntity.created(localizacao).body(OrdemResponse.de(ordem));
    }

    /** {@code GET /ordens/{id}} → 200 ou 404. */
    @GetMapping("/{id}")
    public OrdemResponse consultar(@PathVariable Long id) {
        return OrdemResponse.de(servico.buscarPorId(id));
    }
}
