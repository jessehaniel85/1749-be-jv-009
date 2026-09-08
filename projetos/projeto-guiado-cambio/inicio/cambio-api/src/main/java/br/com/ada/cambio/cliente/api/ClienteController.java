package br.com.ada.cambio.cliente.api;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Borda HTTP do domínio Cliente. */
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService servico;

    public ClienteController(ClienteService servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody ClienteRequest requisicao) {
        // TODO (Aula 1): anotar o corpo com @Valid para que a validação realmente rode
        // TODO (Aula 1): responder 201 com o header Location apontando para /clientes/{cpf}
        //                (dica: receber um UriComponentsBuilder como parâmetro do método)
        Cliente cliente = servico.cadastrar(requisicao.paraEntidade());
        return ResponseEntity.ok(ClienteResponse.de(cliente));
    }

    @GetMapping("/{cpf}")
    public ClienteResponse consultarPorCpf(@PathVariable String cpf) {
        // TODO (Aula 1): 200 quando o cliente existe; 404 quando não — sem try/catch aqui,
        //                quem traduz exceção em status é o TratadorDeErros
        return ClienteResponse.de(servico.buscarPorCpf(cpf));
    }
}
