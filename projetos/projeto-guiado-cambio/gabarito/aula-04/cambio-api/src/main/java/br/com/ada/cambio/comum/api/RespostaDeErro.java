package br.com.ada.cambio.comum.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Corpo padrão de erro da API. Um formato único para todos os erros evita que cada
 * controller invente o seu — é o que o {@code TratadorDeErros} devolve sempre.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RespostaDeErro(
        int status,
        String mensagem,
        LocalDateTime dataHora,
        List<ErroDeCampo> erros) {

    public static RespostaDeErro de(int status, String mensagem) {
        return new RespostaDeErro(status, mensagem, LocalDateTime.now(), List.of());
    }

    public static RespostaDeErro de(int status, String mensagem, List<ErroDeCampo> erros) {
        return new RespostaDeErro(status, mensagem, LocalDateTime.now(), erros);
    }
}
