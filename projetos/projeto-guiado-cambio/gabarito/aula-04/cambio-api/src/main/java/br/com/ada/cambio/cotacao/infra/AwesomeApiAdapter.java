package br.com.ada.cambio.cotacao.infra;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoIndisponivelException;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * <b>Adapter</b> (Anti-Corruption Layer): traduz o modelo da awesomeapi para o nosso.
 *
 * <p>Três incompatibilidades resolvidas aqui, e só aqui:</p>
 * <ol>
 *   <li>a resposta vem <b>chaveada</b> por {@code "USDBRL"} — a chave carrega informação;</li>
 *   <li>o valor vem como {@code String} ({@code "5.4321"}), não como número;</li>
 *   <li>a data vem em {@code yyyy-MM-dd HH:mm:ss}, sem o "T" do ISO-8601.</li>
 * </ol>
 *
 * <p>Se amanhã trocarmos de provedor, é esta classe que se reescreve — o domínio nem fica
 * sabendo. É esse o valor de manter a tradução em um só lugar.</p>
 */
@Component
public class AwesomeApiAdapter {

    private static final DateTimeFormatter FORMATO_EXTERNO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Moeda de destino da conversão: sempre real. */
    private static final String MOEDA_DESTINO = "BRL";

    /**
     * Converte a resposta externa na cotação do nosso domínio.
     *
     * @param moeda    moeda pedida (define a chave esperada no mapa, ex.: {@code USDBRL})
     * @param resposta corpo devolvido pela awesomeapi
     * @throws CotacaoIndisponivelException se a resposta não trouxer a moeda ou o valor
     */
    public Cotacao paraCotacao(Moeda moeda, Map<String, AwesomeApiCotacao> resposta) {
        if (resposta == null || resposta.isEmpty()) {
            throw new CotacaoIndisponivelException(moeda, "provedor externo devolveu resposta vazia");
        }

        AwesomeApiCotacao externa = resposta.get(moeda.name() + MOEDA_DESTINO);
        if (externa == null || externa.bid() == null || externa.bid().isBlank()) {
            throw new CotacaoIndisponivelException(moeda,
                    "provedor externo não devolveu o par " + moeda.name() + MOEDA_DESTINO);
        }

        return new Cotacao(moeda, converterValor(moeda, externa.bid()), converterData(externa.createDate()));
    }

    private BigDecimal converterValor(Moeda moeda, String bid) {
        try {
            return new BigDecimal(bid).setScale(Cotacao.ESCALA_COTACAO, RoundingMode.HALF_EVEN);
        } catch (NumberFormatException erro) {
            throw new CotacaoIndisponivelException(moeda, "valor em formato inesperado: " + bid);
        }
    }

    /** Sem data utilizável, assume-se "agora": a cotação acabou de ser lida. */
    private LocalDateTime converterData(String createDate) {
        if (createDate == null || createDate.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(createDate, FORMATO_EXTERNO);
        } catch (DateTimeParseException erro) {
            return LocalDateTime.now();
        }
    }
}
