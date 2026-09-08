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
