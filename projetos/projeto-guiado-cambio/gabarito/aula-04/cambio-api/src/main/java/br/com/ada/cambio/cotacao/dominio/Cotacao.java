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
 * Valor de 1 unidade da moeda estrangeira em reais, no momento {@code dataHora}.
 *
 * <p>Escala 4 no {@code valorCotacao}: é a precisão com que o mercado publica câmbio.
 * O arredondamento para 2 casas só acontece no <b>total da operação</b>, nunca na cotação —
 * arredondar cedo é como o dinheiro some.</p>
 */
@Entity
@Table(name = "cotacoes")
public class Cotacao {

    /** Casas decimais com que a cotação é publicada. */
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

    /** Registra uma nova cotação para a mesma moeda (usado pelo {@code PUT /cotacoes/{moeda}}). */
    public void atualizar(BigDecimal novoValor, LocalDateTime momento) {
        this.valorCotacao = novoValor;
        this.dataHora = momento;
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
}
