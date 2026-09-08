package br.com.ada.cambio.comum.api;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz exceções em respostas HTTP, em um lugar só.
 *
 * <p>Sem isto, cada controller precisaria de {@code try/catch} — e a regra de "qual erro vira
 * qual status" ficaria espalhada pelo projeto inteiro.</p>
 */
@RestControllerAdvice
public class TratadorDeErros {

    // TODO (Aula 1): MethodArgumentNotValidException  → 400 com a lista de campos inválidos
    //                (use RespostaDeErro + ErroDeCampo, já prontos neste pacote)
    // TODO (Aula 1): ClienteNaoEncontradoException    → 404
    // TODO (Aula 1): CpfDuplicadoException            → 409
    // TODO (Aula 1): HttpMessageNotReadableException  → 400 (JSON malformado ou enum inexistente)
}
