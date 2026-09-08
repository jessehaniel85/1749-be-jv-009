package br.com.ada.cambio.comum.api;

import br.com.ada.cambio.cliente.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.cliente.dominio.CpfDuplicadoException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz exceções em respostas HTTP.
 *
 * <p>Sem isto, cada controller teria um {@code try/catch} — e a regra de "qual erro vira
 * qual status" ficaria espalhada. Aqui ela mora em um lugar só.</p>
 */
@RestControllerAdvice
public class TratadorDeErros {

    /** Falha de validação do corpo (Bean Validation) → 400 com a lista de campos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaDeErro> tratarValidacao(MethodArgumentNotValidException excecao) {
        List<ErroDeCampo> erros = excecao.getBindingResult().getFieldErrors().stream()
                .map(this::paraErroDeCampo)
                .toList();
        return ResponseEntity.badRequest()
                .body(RespostaDeErro.de(HttpStatus.BAD_REQUEST.value(), "Requisição inválida", erros));
    }

    /** JSON malformado ou valor de enum inexistente → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaDeErro> tratarCorpoIlegivel(HttpMessageNotReadableException excecao) {
        return ResponseEntity.badRequest()
                .body(RespostaDeErro.de(HttpStatus.BAD_REQUEST.value(),
                        "Corpo da requisição inválido ou mal formado"));
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<RespostaDeErro> tratarClienteNaoEncontrado(ClienteNaoEncontradoException excecao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RespostaDeErro.de(HttpStatus.NOT_FOUND.value(), excecao.getMessage()));
    }

    @ExceptionHandler(CpfDuplicadoException.class)
    public ResponseEntity<RespostaDeErro> tratarCpfDuplicado(CpfDuplicadoException excecao) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(RespostaDeErro.de(HttpStatus.CONFLICT.value(), excecao.getMessage()));
    }

    private ErroDeCampo paraErroDeCampo(FieldError erro) {
        return new ErroDeCampo(erro.getField(), erro.getDefaultMessage());
    }
}
