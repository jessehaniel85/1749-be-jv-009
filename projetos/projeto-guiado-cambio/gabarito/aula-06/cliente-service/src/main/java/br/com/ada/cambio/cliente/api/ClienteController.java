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

/** API HTTP do dominio Cliente. */
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest requisicao) {
        Cliente salvo = clienteService.cadastrar(requisicao.paraEntidade());
        return ResponseEntity
                .created(URI.create("/clientes/" + salvo.getCpf()))
                .body(ClienteResponse.de(salvo));
    }

    @GetMapping("/{cpf}")
    public ClienteResponse buscarPorCpf(@PathVariable String cpf) {
        return ClienteResponse.de(clienteService.buscarPorCpf(cpf));
    }
}
