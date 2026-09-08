package br.com.ada.cambio.cliente.dominio;

import br.com.ada.cambio.cliente.infra.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negócio do cadastro de clientes.
 *
 * <p>Injeção por construtor (e não em campo): é o que vai permitir testar esta classe com
 * Mockito, sem subir contexto Spring, na Aula 2.</p>
 */
@Service
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Cliente cadastrar(Cliente cliente) {
        // TODO (Aula 1): se o CPF já existir no repositório, lançar CpfDuplicadoException (→ 409)
        return repositorio.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorCpf(String cpf) {
        // TODO (Aula 1): buscar por CPF no repositório;
        //                se não encontrar, lançar ClienteNaoEncontradoException (→ 404)
        throw new UnsupportedOperationException("TODO Aula 1: implementar a busca por CPF");
    }
}
