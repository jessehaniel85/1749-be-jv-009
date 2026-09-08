package br.com.ada.cambio.cliente.dominio;

import br.com.ada.cambio.cliente.infra.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacao das duas portas do dominio Cliente.
 *
 * <p>Uma classe pode implementar varias interfaces pequenas - o ISP fala do que
 * o CONSUMIDOR enxerga, nao do numero de classes.</p>
 */
@Service
public class ClienteService implements ConsultaCliente, CadastroCliente {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public Cliente cadastrar(Cliente cliente) {
        recusarCpfJaCadastrado(cliente.getCpf());
        return repositorio.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorCpf(String cpf) {
        return repositorio.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
    }

    private void recusarCpfJaCadastrado(String cpf) {
        if (repositorio.existsByCpf(cpf)) {
            throw new CpfDuplicadoException(cpf);
        }
    }
}
