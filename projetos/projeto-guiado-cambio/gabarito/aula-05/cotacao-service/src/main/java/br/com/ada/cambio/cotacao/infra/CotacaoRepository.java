package br.com.ada.cambio.cotacao.infra;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a tabela local de cotacoes (base H2 propria do cotacao-service). */
public interface CotacaoRepository extends JpaRepository<Cotacao, Long> {

    Optional<Cotacao> findByMoeda(Moeda moeda);
}
