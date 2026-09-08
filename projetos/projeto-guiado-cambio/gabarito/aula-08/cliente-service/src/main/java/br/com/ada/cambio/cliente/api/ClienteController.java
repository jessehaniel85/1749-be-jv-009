package br.com.ada.cambio.cliente.api;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.CadastroCliente;
import br.com.ada.cambio.cliente.dominio.ConsultaCliente;
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

    private final CadastroCliente cadastroCliente;
    private final ConsultaCliente consultaCliente;

    // Injecao POR CONSTRUTOR e as duas portas separadas: o controller declara,
    // no proprio construtor, exatamente o que ele usa - nada mais.
    public ClienteController(CadastroCliente cadastroCliente, ConsultaCliente consultaCliente) {
        this.cadastroCliente = cadastroCliente;
        this.consultaCliente = consultaCliente;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest requisicao) {
        Cliente salvo = cadastroCliente.cadastrar(requisicao.paraEntidade());
        return ResponseEntity
                .created(URI.create("/clientes/" + salvo.getCpf()))
                .body(ClienteResponse.de(salvo));
    }

    @GetMapping("/{cpf}")
    public ClienteResponse buscarPorCpf(@PathVariable String cpf) {
        return ClienteResponse.de(consultaCliente.buscarPorCpf(cpf));
    }
}
