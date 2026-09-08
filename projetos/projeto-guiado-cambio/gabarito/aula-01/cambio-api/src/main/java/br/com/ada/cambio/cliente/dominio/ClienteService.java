package br.com.ada.cambio.cliente.dominio;

import br.com.ada.cambio.cliente.infra.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negócio do cadastro de clientes.
 *
 * <p>Injeção por construtor (e não em campo): é o que permite testar a classe com
 * Mockito sem subir contexto Spring — ver {@code ClienteServiceTest} na Aula 2.</p>
 */
@Service
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Cadastra um novo cliente.
     *
     * @throws CpfDuplicadoException se o CPF já estiver cadastrado
     */
    @Transactional
    public Cliente cadastrar(Cliente cliente) {
        if (repositorio.existsByCpf(cliente.getCpf())) {
            throw new CpfDuplicadoException(cliente.getCpf());
        }
        return repositorio.save(cliente);
    }

    /**
     * Busca um cliente pelo CPF.
     *
     * @throws ClienteNaoEncontradoException se não houver cliente com esse CPF
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorCpf(String cpf) {
        return repositorio.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
    }
}
