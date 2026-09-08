package br.com.ada.cambio.comum.api;

import br.com.ada.cambio.cliente.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.cliente.dominio.CpfDuplicadoException;
import br.com.ada.cambio.cotacao.dominio.MoedaNaoSuportadaException;
import br.com.ada.cambio.ordem.dominio.AgenciaInvalidaException;
import br.com.ada.cambio.ordem.dominio.OrdemNaoEncontradaException;
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
 *
 * <p><b>400 × 422:</b> 400 é requisição malformada (o servidor não conseguiu entender);
 * 422 é requisição bem formada cujo conteúdo o negócio recusa (moeda que não operamos,
 * agência que não existe no formato esperado).</p>
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
        return naoEncontrado(excecao.getMessage());
    }

    @ExceptionHandler(OrdemNaoEncontradaException.class)
    public ResponseEntity<RespostaDeErro> tratarOrdemNaoEncontrada(OrdemNaoEncontradaException excecao) {
        return naoEncontrado(excecao.getMessage());
    }

    @ExceptionHandler(CpfDuplicadoException.class)
    public ResponseEntity<RespostaDeErro> tratarCpfDuplicado(CpfDuplicadoException excecao) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(RespostaDeErro.de(HttpStatus.CONFLICT.value(), excecao.getMessage()));
    }

    @ExceptionHandler(MoedaNaoSuportadaException.class)
    public ResponseEntity<RespostaDeErro> tratarMoedaNaoSuportada(MoedaNaoSuportadaException excecao) {
        return naoProcessavel(excecao.getMessage());
    }

    @ExceptionHandler(AgenciaInvalidaException.class)
    public ResponseEntity<RespostaDeErro> tratarAgenciaInvalida(AgenciaInvalidaException excecao) {
        return naoProcessavel(excecao.getMessage());
    }

    private ResponseEntity<RespostaDeErro> naoEncontrado(String mensagem) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RespostaDeErro.de(HttpStatus.NOT_FOUND.value(), mensagem));
    }

    private ResponseEntity<RespostaDeErro> naoProcessavel(String mensagem) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(RespostaDeErro.de(HttpStatus.UNPROCESSABLE_ENTITY.value(), mensagem));
    }

    private ErroDeCampo paraErroDeCampo(FieldError erro) {
        return new ErroDeCampo(erro.getField(), erro.getDefaultMessage());
    }
}
