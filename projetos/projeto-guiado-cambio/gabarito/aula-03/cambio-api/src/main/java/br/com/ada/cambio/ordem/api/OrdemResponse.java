package br.com.ada.cambio.ordem.api;

import br.com.ada.cambio.cotacao.dominio.Moeda;
import br.com.ada.cambio.ordem.dominio.OrdemDeCompra;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Comprovante da ordem de compra, no contrato acordado com o time de app.
 *
 * <p>O JSON é <b>snake_case</b> (menos {@code dataSolicitacao}, que ficou em camelCase no
 * contrato original) — e o Java continua camelCase. É exatamente para isso que serve o DTO:
 * a entidade não precisa se contorcer para agradar o consumidor da API, e o consumidor não
 * precisa saber como nomeamos nossos campos.</p>
 */
public record OrdemResponse(

        @JsonProperty("id_compra")
        Long idCompra,

        @JsonProperty("id_cliente")
        Long idCliente,

        @JsonProperty("cpf_cliente")
        String cpfCliente,

        @JsonProperty("dataSolicitacao")
        LocalDateTime dataSolicitacao,

        @JsonProperty("tipo_moeda")
        Moeda tipoMoeda,

        @JsonProperty("valor_moeda_estrangeira")
        BigDecimal valorMoedaEstrangeira,

        @JsonProperty("valor_cotacao")
        BigDecimal valorCotacao,

        @JsonProperty("valor_total_operacao")
        BigDecimal valorTotalOperacao,

        @JsonProperty("numero_agencia_retirada")
        String numeroAgenciaRetirada) {

    public static OrdemResponse de(OrdemDeCompra ordem) {
        return new OrdemResponse(
                ordem.getId(),
                ordem.getIdCliente(),
                ordem.getCpfCliente(),
                ordem.getDataSolicitacao(),
                ordem.getMoeda(),
                ordem.getValorMoedaEstrangeira(),
                ordem.getValorCotacao(),
                ordem.getValorTotalOperacao(),
                ordem.getNumeroAgenciaRetirada());
    }
}
