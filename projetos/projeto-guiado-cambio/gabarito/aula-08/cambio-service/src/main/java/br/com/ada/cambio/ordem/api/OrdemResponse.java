package br.com.ada.cambio.ordem.api;

import br.com.ada.cambio.ordem.dominio.Moeda;
import br.com.ada.cambio.ordem.dominio.OrdemDeCompra;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Comprovante da ordem, no contrato snake_case definido no brief do modulo.
 *
 * <p>{@code dataSolicitacao} fica em camelCase de proposito: e assim que o
 * contrato oficial do modulo esta escrito.</p>
 *
 * <h2>BUILDER (opcional, didatico)</h2>
 * <p>Nove componentes, tres deles {@code BigDecimal} vizinhos. O construtor
 * canonico compila com {@code valorCotacao} e {@code valorTotalOperacao}
 * trocados de lugar — e o bug so aparece na tela do cliente.</p>
 *
 * <p>O {@link Construtor} abaixo resolve isso dando um NOME a cada valor no
 * ponto da chamada. Nao e obrigatorio: {@link #de(OrdemDeCompra)} continua
 * disponivel. E o caso classico de Builder — muitos parametros do mesmo tipo,
 * onde a ordem e a unica coisa que separa o certo do errado.</p>
 */
public record OrdemResponse(

        @JsonProperty("id_compra") Long idCompra,
        @JsonProperty("id_cliente") Long idCliente,
        @JsonProperty("cpf_cliente") String cpfCliente,
        @JsonProperty("dataSolicitacao") LocalDateTime dataSolicitacao,
        @JsonProperty("tipo_moeda") Moeda tipoMoeda,
        @JsonProperty("valor_moeda_estrangeira") BigDecimal valorMoedaEstrangeira,
        @JsonProperty("valor_cotacao") BigDecimal valorCotacao,
        @JsonProperty("valor_total_operacao") BigDecimal valorTotalOperacao,
        @JsonProperty("numero_agencia_retirada") String numeroAgenciaRetirada) {

    /** Conversao a partir da entidade — o caminho usado pelo controller. */
    public static OrdemResponse de(OrdemDeCompra ordem) {
        return construtor()
                .idCompra(ordem.getId())
                .idCliente(ordem.getIdCliente())
                .cpfCliente(ordem.getCpfCliente())
                .dataSolicitacao(ordem.getDataSolicitacao())
                .tipoMoeda(ordem.getMoeda())
                .valorMoedaEstrangeira(ordem.getValorMoedaEstrangeira())
                .valorCotacao(ordem.getValorCotacao())
                .valorTotalOperacao(ordem.getValorTotalOperacao())
                .numeroAgenciaRetirada(ordem.getNumeroAgenciaRetirada())
                .construir();
    }

    public static Construtor construtor() {
        return new Construtor();
    }

    /** BUILDER: cada valor entra com nome; a ordem das chamadas nao importa. */
    public static final class Construtor {

        private Long idCompra;
        private Long idCliente;
        private String cpfCliente;
        private LocalDateTime dataSolicitacao;
        private Moeda tipoMoeda;
        private BigDecimal valorMoedaEstrangeira;
        private BigDecimal valorCotacao;
        private BigDecimal valorTotalOperacao;
        private String numeroAgenciaRetirada;

        private Construtor() {
        }

        public Construtor idCompra(Long idCompra) {
            this.idCompra = idCompra;
            return this;
        }

        public Construtor idCliente(Long idCliente) {
            this.idCliente = idCliente;
            return this;
        }

        public Construtor cpfCliente(String cpfCliente) {
            this.cpfCliente = cpfCliente;
            return this;
        }

        public Construtor dataSolicitacao(LocalDateTime dataSolicitacao) {
            this.dataSolicitacao = dataSolicitacao;
            return this;
        }

        public Construtor tipoMoeda(Moeda tipoMoeda) {
            this.tipoMoeda = tipoMoeda;
            return this;
        }

        public Construtor valorMoedaEstrangeira(BigDecimal valorMoedaEstrangeira) {
            this.valorMoedaEstrangeira = valorMoedaEstrangeira;
            return this;
        }

        public Construtor valorCotacao(BigDecimal valorCotacao) {
            this.valorCotacao = valorCotacao;
            return this;
        }

        public Construtor valorTotalOperacao(BigDecimal valorTotalOperacao) {
            this.valorTotalOperacao = valorTotalOperacao;
            return this;
        }

        public Construtor numeroAgenciaRetirada(String numeroAgenciaRetirada) {
            this.numeroAgenciaRetirada = numeroAgenciaRetirada;
            return this;
        }

        public OrdemResponse construir() {
            return new OrdemResponse(idCompra, idCliente, cpfCliente, dataSolicitacao,
                    tipoMoeda, valorMoedaEstrangeira, valorCotacao, valorTotalOperacao,
                    numeroAgenciaRetirada);
        }
    }
}
