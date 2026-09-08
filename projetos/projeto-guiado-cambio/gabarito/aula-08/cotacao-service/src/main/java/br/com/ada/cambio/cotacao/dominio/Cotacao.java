package br.com.ada.cambio.cotacao.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Valor de 1 unidade da moeda estrangeira em BRL, com o instante da apuracao.
 *
 * <p>A escala 4 e proposital: cotacao tem quatro casas (ex.: 6.5857). O
 * arredondamento para 2 casas so acontece no total da operacao, no cambio-service.</p>
 *
 * <p>Instancias vindas do provedor externo nao sao persistidas: chegam com
 * {@code id} nulo e sao devolvidas direto ao chamador.</p>
 */
@Entity
@Table(name = "cotacoes")
public class Cotacao {

    public static final int ESCALA_COTACAO = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 3)
    private Moeda moeda;

    @Column(name = "valor_cotacao", nullable = false, precision = 19, scale = ESCALA_COTACAO)
    private BigDecimal valorCotacao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    /** Construtor exigido pelo JPA. */
    protected Cotacao() {
    }

    public Cotacao(Moeda moeda, BigDecimal valorCotacao, LocalDateTime dataHora) {
        this.moeda = moeda;
        this.valorCotacao = valorCotacao;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public Moeda getMoeda() {
        return moeda;
    }

    public BigDecimal getValorCotacao() {
        return valorCotacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    /** Atualiza a cotacao mantendo a identidade da linha (usado pelo PUT). */
    public void atualizar(BigDecimal novoValor, LocalDateTime momento) {
        this.valorCotacao = novoValor;
        this.dataHora = momento;
    }
}
