package br.com.ada.cambio.ordem.infra;

import br.com.ada.cambio.ordem.dominio.OrdemDeCompra;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso à tabela {@code ordens_de_compra}. */
public interface OrdemRepository extends JpaRepository<OrdemDeCompra, Long> {
}
