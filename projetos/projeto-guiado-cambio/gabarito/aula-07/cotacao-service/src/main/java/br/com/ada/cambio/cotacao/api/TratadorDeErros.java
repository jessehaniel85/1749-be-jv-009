package br.com.ada.cambio.cotacao.api;

import br.com.ada.cambio.cotacao.dominio.CotacaoIndisponivelException;
import br.com.ada.cambio.cotacao.dominio.MoedaNaoSuportadaException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz excecoes de dominio em status HTTP.
 *
 * <p>400 validacao - 422 moeda nao suportada - 503 fonte de cotacao fora do ar.</p>
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException excecao) {
        String detalhes = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(ErroResposta.de(HttpStatus.BAD_REQUEST.value(), "Requisicao invalida", detalhes));
    }

    @ExceptionHandler(MoedaNaoSuportadaException.class)
    public ResponseEntity<ErroResposta> tratarMoedaNaoSuportada(MoedaNaoSuportadaException excecao) {
        return ResponseEntity.unprocessableEntity()
                .body(ErroResposta.de(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Moeda nao suportada", excecao.getMessage()));
    }

    @ExceptionHandler(CotacaoIndisponivelException.class)
    public ResponseEntity<ErroResposta> tratarIndisponivel(CotacaoIndisponivelException excecao) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErroResposta.de(HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Cotacao indisponivel", excecao.getMessage()));
    }
}
