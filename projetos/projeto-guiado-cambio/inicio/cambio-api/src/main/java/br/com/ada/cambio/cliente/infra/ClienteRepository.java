package br.com.ada.cambio.cliente.infra;

import br.com.ada.cambio.cliente.dominio.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso à tabela {@code clientes}. O Spring Data implementa a interface em tempo de
 * execução — inclusive as consultas derivadas do nome do método.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // TODO (Aula 1): Optional<Cliente> findByCpf(String cpf);
    // TODO (Aula 1): boolean existsByCpf(String cpf);
}
