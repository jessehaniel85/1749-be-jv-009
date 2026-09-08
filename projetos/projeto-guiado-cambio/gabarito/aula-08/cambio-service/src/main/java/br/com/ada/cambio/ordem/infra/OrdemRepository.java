package br.com.ada.cambio.ordem.infra;

import br.com.ada.cambio.ordem.dominio.OrdemDeCompra;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a base H2 propria do cambio-service. */
public interface OrdemRepository extends JpaRepository<OrdemDeCompra, Long> {
}
