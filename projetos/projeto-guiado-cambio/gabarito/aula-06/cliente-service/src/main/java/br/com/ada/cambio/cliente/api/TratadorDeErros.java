package br.com.ada.cambio.cliente.api;

import br.com.ada.cambio.cliente.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.cliente.dominio.CpfDuplicadoException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz excecoes de dominio em status HTTP.
 *
 * <p>400 validacao - 404 nao encontrado - 409 CPF duplicado.</p>
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

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(ClienteNaoEncontradoException excecao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResposta.de(HttpStatus.NOT_FOUND.value(), "Cliente nao encontrado", excecao.getMessage()));
    }

    @ExceptionHandler(CpfDuplicadoException.class)
    public ResponseEntity<ErroResposta> tratarCpfDuplicado(CpfDuplicadoException excecao) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResposta.de(HttpStatus.CONFLICT.value(), "CPF duplicado", excecao.getMessage()));
    }
}
