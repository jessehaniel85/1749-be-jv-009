package br.com.ada.cambio.ordem.api;

import br.com.ada.cambio.ordem.dominio.NovaOrdem;
import br.com.ada.cambio.ordem.dominio.RegrasDeCambio;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * Corpo de entrada de {@code POST /ordens}, em snake_case como o contrato do modulo.
 *
 * <p>Os {@code @JsonAlias} aceitam tambem o nome em camelCase - util para quem
 * testa pelo Postman com o corpo antigo do monolito.</p>
 *
 * <p>Repare no que NAO tem validacao aqui: moeda e agencia sao verificadas no
 * dominio, porque o contrato manda responder 422 (regra de negocio) e nao 400
 * (formato).</p>
 */
public record OrdemRequest(

        @JsonProperty("cpf_cliente")
        @JsonAlias("cpfCliente")
        @NotBlank(message = "cpf_cliente e obrigatorio")
        @Pattern(regexp = RegrasDeCambio.PADRAO_CPF, message = "cpf_cliente deve conter exatamente 11 digitos")
        String cpfCliente,

        @JsonProperty("tipo_moeda")
        @JsonAlias({"moeda", "tipoMoeda"})
        @NotBlank(message = "tipo_moeda e obrigatorio")
        String tipoMoeda,

        @JsonProperty("valor_moeda_estrangeira")
        @JsonAlias("valorMoedaEstrangeira")
        @NotNull(message = "valor_moeda_estrangeira e obrigatorio")
        @DecimalMin(value = "0.01", message = "valor_moeda_estrangeira deve ser maior que zero")
        BigDecimal valorMoedaEstrangeira,

        @JsonProperty("numero_agencia_retirada")
        @JsonAlias("numeroAgenciaRetirada")
        @NotBlank(message = "numero_agencia_retirada e obrigatorio")
        String numeroAgenciaRetirada) {

    /** Converte o DTO de borda no comando do dominio. A traducao mora na borda. */
    public NovaOrdem paraNovaOrdem() {
        return new NovaOrdem(cpfCliente, tipoMoeda, valorMoedaEstrangeira, numeroAgenciaRetirada);
    }
}
