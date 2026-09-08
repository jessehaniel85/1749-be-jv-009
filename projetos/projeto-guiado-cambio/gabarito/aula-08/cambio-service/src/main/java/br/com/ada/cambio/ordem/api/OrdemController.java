package br.com.ada.cambio.ordem.api;

import br.com.ada.cambio.ordem.dominio.CambioFacade;
import br.com.ada.cambio.ordem.dominio.OrdemDeCompra;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API HTTP do dominio Ordem de compra.
 *
 * <p>Uma unica dependencia: a {@link CambioFacade}. O controller nao sabe que
 * existem validador, dois servicos remotos, estrategias por moeda e um
 * repositorio — e nao precisa saber.</p>
 */
@RestController
@RequestMapping("/ordens")
public class OrdemController {

    private final CambioFacade cambioFacade;

    public OrdemController(CambioFacade cambioFacade) {
        this.cambioFacade = cambioFacade;
    }

    @PostMapping
    public ResponseEntity<OrdemResponse> registrar(@Valid @RequestBody OrdemRequest requisicao) {
        OrdemDeCompra ordem = cambioFacade.registrar(requisicao.paraNovaOrdem());
        return ResponseEntity
                .created(URI.create("/ordens/" + ordem.getId()))
                .body(OrdemResponse.de(ordem));
    }

    @GetMapping("/{id}")
    public OrdemResponse buscarPorId(@PathVariable Long id) {
        return OrdemResponse.de(cambioFacade.consultarPorId(id));
    }
}
