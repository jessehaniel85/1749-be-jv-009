package br.com.ada.cambio.cliente.infra;

import br.com.ada.cambio.cliente.dominio.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a base H2 local do cliente-service. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
