package br.com.ada.cambio.cotacao.infra;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso à tabela {@code cotacoes}, semeada por {@code data.sql}. */
public interface CotacaoRepository extends JpaRepository<Cotacao, Long> {

    Optional<Cotacao> findByMoeda(Moeda moeda);
}
