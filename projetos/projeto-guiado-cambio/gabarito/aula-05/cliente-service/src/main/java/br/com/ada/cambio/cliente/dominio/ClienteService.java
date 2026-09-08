package br.com.ada.cambio.cliente.dominio;

import br.com.ada.cambio.cliente.infra.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de cadastro e consulta de clientes. */
@Service
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Cadastra um cliente novo.
     *
     * @throws CpfDuplicadoException se o CPF ja estiver em uso
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
     * @throws ClienteNaoEncontradoException se nao houver cliente com esse CPF
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorCpf(String cpf) {
        return repositorio.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
    }
}
