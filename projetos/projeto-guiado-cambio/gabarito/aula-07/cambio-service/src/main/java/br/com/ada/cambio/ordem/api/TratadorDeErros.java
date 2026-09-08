package br.com.ada.cambio.ordem.api;

import br.com.ada.cambio.ordem.dominio.AgenciaInvalidaException;
import br.com.ada.cambio.ordem.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.ordem.dominio.MoedaNaoSuportadaException;
import br.com.ada.cambio.ordem.dominio.OrdemNaoEncontradaException;
import br.com.ada.cambio.ordem.dominio.ServicoIndisponivelException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz excecoes DE DOMINIO em status HTTP.
 *
 * <p><b>O ganho da Aula 7 esta na lista de imports.</b> Compare com a Aula 6:
 * sumiram {@code feign.FeignException}, {@code feign.RetryableException} e o
 * {@code IllegalStateException} com {@code if} na mensagem. Este arquivo agora
 * so conhece o vocabulario do negocio.</p>
 *
 * <p>O comportamento observavel e <b>identico</b> ao da Aula 6 — mesmos status,
 * para os mesmos casos. Foi refatoracao, nao mudanca.</p>
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException excecao) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisicao invalida", descrever(excecao));
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarClienteInexistente(ClienteNaoEncontradoException excecao) {
        return resposta(HttpStatus.NOT_FOUND, "Cliente nao encontrado", excecao.getMessage());
    }

    @ExceptionHandler(OrdemNaoEncontradaException.class)
    public ResponseEntity<ErroResposta> tratarOrdemInexistente(OrdemNaoEncontradaException excecao) {
        return resposta(HttpStatus.NOT_FOUND, "Ordem nao encontrada", excecao.getMessage());
    }

    @ExceptionHandler(MoedaNaoSuportadaException.class)
    public ResponseEntity<ErroResposta> tratarMoeda(MoedaNaoSuportadaException excecao) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, "Moeda nao suportada", excecao.getMessage());
    }

    @ExceptionHandler(AgenciaInvalidaException.class)
    public ResponseEntity<ErroResposta> tratarAgencia(AgenciaInvalidaException excecao) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, "Agencia invalida", excecao.getMessage());
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErroResposta> tratarIndisponivel(ServicoIndisponivelException excecao) {
        // 503 para o usuario, causa completa no log: o operador precisa saber
        // QUAL vizinho falhou e por que; o usuario, nao.
        LOG.error("Servico vizinho indisponivel", excecao);
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, "Servico indisponivel", excecao.getMessage());
    }

    private String descrever(MethodArgumentNotValidException excecao) {
        return excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }

    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(ErroResposta.de(status.value(), erro, mensagem));
    }
}
