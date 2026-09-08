package br.com.ada.cambio.cliente.api;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.ClienteService;
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

/** Borda HTTP do domínio Cliente. */
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService servico;

    public ClienteController(ClienteService servico) {
        this.servico = servico;
    }

    /** {@code POST /clientes} → 201 com o header {@code Location} apontando para a consulta por CPF. */
    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody @Valid ClienteRequest requisicao,
                                                     UriComponentsBuilder construtorDeUri) {
        Cliente cliente = servico.cadastrar(requisicao.paraEntidade());
        URI localizacao = construtorDeUri.path("/clientes/{cpf}")
                .buildAndExpand(cliente.getCpf())
                .toUri();
        return ResponseEntity.created(localizacao).body(ClienteResponse.de(cliente));
    }

    /** {@code GET /clientes/{cpf}} → 200 ou 404. */
    @GetMapping("/{cpf}")
    public ClienteResponse consultarPorCpf(@PathVariable String cpf) {
        return ClienteResponse.de(servico.buscarPorCpf(cpf));
    }
}
