package br.com.ada.cambio.ordem.dominio;

import br.com.ada.cambio.cotacao.dominio.Moeda;
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
 * Ordem de compra de moeda estrangeira, com retirada em agência.
 *
 * <p>Guarda a <b>cotação usada no momento da compra</b>, e não só uma referência à moeda:
 * a cotação muda a cada minuto e o comprovante precisa ser reproduzível meses depois.
 * O mesmo vale para {@code cpfCliente} — o comprovante não deve depender de um JOIN.</p>
 */
@Entity
@Table(name = "ordens_de_compra")
public class OrdemDeCompra {

    /** Quantidade de dígitos do número de agência. */
    public static final int TAMANHO_AGENCIA = 4;

    /** Casas decimais do valor final em reais. */
    public static final int ESCALA_MONETARIA = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "cpf_cliente", nullable = false, length = 11)
    private String cpfCliente;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Moeda moeda;

    @Column(name = "valor_moeda_estrangeira", nullable = false, precision = 19, scale = ESCALA_MONETARIA)
    private BigDecimal valorMoedaEstrangeira;

    @Column(name = "valor_cotacao", nullable = false, precision = 19, scale = 4)
    private BigDecimal valorCotacao;

    @Column(name = "valor_total_operacao", nullable = false, precision = 19, scale = ESCALA_MONETARIA)
    private BigDecimal valorTotalOperacao;

    @Column(name = "numero_agencia_retirada", nullable = false, length = TAMANHO_AGENCIA)
    private String numeroAgenciaRetirada;

    /** Construtor exigido pelo JPA. */
    protected OrdemDeCompra() {
    }

    public OrdemDeCompra(Long idCliente,
                         String cpfCliente,
                         LocalDateTime dataSolicitacao,
                         Moeda moeda,
                         BigDecimal valorMoedaEstrangeira,
                         BigDecimal valorCotacao,
                         BigDecimal valorTotalOperacao,
                         String numeroAgenciaRetirada) {
        this.idCliente = idCliente;
        this.cpfCliente = cpfCliente;
        this.dataSolicitacao = dataSolicitacao;
        this.moeda = moeda;
        this.valorMoedaEstrangeira = valorMoedaEstrangeira;
        this.valorCotacao = valorCotacao;
        this.valorTotalOperacao = valorTotalOperacao;
        this.numeroAgenciaRetirada = numeroAgenciaRetirada;
    }

    public Long getId() {
        return id;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public Moeda getMoeda() {
        return moeda;
    }

    public BigDecimal getValorMoedaEstrangeira() {
        return valorMoedaEstrangeira;
    }

    public BigDecimal getValorCotacao() {
        return valorCotacao;
    }

    public BigDecimal getValorTotalOperacao() {
        return valorTotalOperacao;
    }

    public String getNumeroAgenciaRetirada() {
        return numeroAgenciaRetirada;
    }
}
