package br.com.ada.cambio.cotacao.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Espelho fiel de <b>um pedaço do JSON da awesomeapi</b> — nada mais.
 *
 * <p>Formato recebido de {@code GET /last/USD-BRL}:</p>
 * <pre>
 * { "USDBRL": { "code":"USD", "codein":"BRL", "bid":"5.4321",
 *               "create_date":"2026-09-08 10:31:02", ... } }
 * </pre>
 *
 * <p>Esta classe fica em {@code infra} e <b>não</b> vaza para o domínio: o nome esquisito
 * do campo ({@code bid}), o valor em {@code String} e a data sem "T" são problemas do
 * provedor externo. Quem resolve isso é o {@link AwesomeApiAdapter}.</p>
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)}: se a awesomeapi adicionar um campo
 * amanhã, nossa integração não quebra.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AwesomeApiCotacao(

        String code,

        String codein,

        String bid,

        @JsonProperty("create_date")
        String createDate) {
}
