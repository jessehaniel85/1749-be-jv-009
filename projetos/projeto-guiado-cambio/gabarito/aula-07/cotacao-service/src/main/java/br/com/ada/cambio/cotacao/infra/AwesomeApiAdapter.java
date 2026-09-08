package br.com.ada.cambio.cotacao.infra;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoIndisponivelException;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Component;

/**
 * ADAPTER (camada anticorrupcao): traduz o JSON da awesomeapi para o modelo
 * interno {@link Cotacao}.
 *
 * <p>Se a awesomeapi mudar o nome dos campos, so esta classe muda.</p>
 */
@Component
public class AwesomeApiAdapter {

    private static final DateTimeFormatter FORMATO_EXTERNO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Cotacao paraCotacao(Moeda moeda, AwesomeApiResposta resposta) {
        if (resposta == null || resposta.bid() == null) {
            throw new CotacaoIndisponivelException(
                    "Resposta da awesomeapi sem o campo 'bid' para " + moeda);
        }
        return new Cotacao(moeda, new BigDecimal(resposta.bid()), converterDataHora(resposta.create_date()));
    }

    private LocalDateTime converterDataHora(String dataHoraExterna) {
        if (dataHoraExterna == null || dataHoraExterna.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(dataHoraExterna, FORMATO_EXTERNO);
        } catch (DateTimeParseException excecao) {
            return LocalDateTime.now();
        }
    }
}
