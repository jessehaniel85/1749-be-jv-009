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

/** API HTTP do dominio Ordem de compra. */
@RestController
@RequestMapping("/ordens")
public class OrdemController {

    private final OrdemService ordemService;

    public OrdemController(OrdemService ordemService) {
        this.ordemService = ordemService;
    }

    @PostMapping
    public ResponseEntity<OrdemResponse> registrar(@Valid @RequestBody OrdemRequest requisicao) {
        OrdemDeCompra ordem = ordemService.registrar(
                requisicao.cpfCliente(),
                requisicao.tipoMoeda(),
                requisicao.valorMoedaEstrangeira(),
                requisicao.numeroAgenciaRetirada());
        return ResponseEntity
                .created(URI.create("/ordens/" + ordem.getId()))
                .body(OrdemResponse.de(ordem));
    }

    @GetMapping("/{id}")
    public OrdemResponse buscarPorId(@PathVariable Long id) {
        return OrdemResponse.de(ordemService.buscarPorId(id));
    }
}
