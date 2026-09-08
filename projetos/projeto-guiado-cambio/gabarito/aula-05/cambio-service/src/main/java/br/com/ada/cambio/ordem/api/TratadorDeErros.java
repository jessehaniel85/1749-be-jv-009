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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Traduz excecoes em status HTTP.
 *
 * <p>400 validacao - 404 nao encontrado - 422 moeda/agencia - 503 vizinho fora do ar.</p>
 *
 * <p>Os dois ultimos handlers sao a novidade da Aula 5: agora existe REDE entre
 * nos e o resto do sistema, e rede falha.</p>
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException excecao) {
        String detalhes = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(ErroResposta.de(HttpStatus.BAD_REQUEST.value(), "Requisicao invalida", detalhes));
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarClienteInexistente(ClienteNaoEncontradoException excecao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResposta.de(HttpStatus.NOT_FOUND.value(), "Cliente nao encontrado", excecao.getMessage()));
    }

    @ExceptionHandler(OrdemNaoEncontradaException.class)
    public ResponseEntity<ErroResposta> tratarOrdemInexistente(OrdemNaoEncontradaException excecao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResposta.de(HttpStatus.NOT_FOUND.value(), "Ordem nao encontrada", excecao.getMessage()));
    }

    @ExceptionHandler(MoedaNaoSuportadaException.class)
    public ResponseEntity<ErroResposta> tratarMoeda(MoedaNaoSuportadaException excecao) {
        return ResponseEntity.unprocessableEntity()
                .body(ErroResposta.de(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Moeda nao suportada", excecao.getMessage()));
    }

    @ExceptionHandler(AgenciaInvalidaException.class)
    public ResponseEntity<ErroResposta> tratarAgencia(AgenciaInvalidaException excecao) {
        return ResponseEntity.unprocessableEntity()
                .body(ErroResposta.de(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Agencia invalida", excecao.getMessage()));
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErroResposta> tratarIndisponivel(ServicoIndisponivelException excecao) {
        LOG.error("Servico indisponivel", excecao);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErroResposta.de(HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Servico indisponivel", excecao.getMessage()));
    }

    /** Vizinho nao respondeu: conexao recusada, DNS, timeout. */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErroResposta> tratarVizinhoForaDoAr(ResourceAccessException excecao) {
        // 503 para o usuario, causa no log: o operador precisa saber QUAL vizinho
        // falhou; o usuario, nao.
        LOG.error("Vizinho nao respondeu", excecao);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErroResposta.de(HttpStatus.SERVICE_UNAVAILABLE.value(), "Servico indisponivel",
                        "Um servico do qual dependemos nao respondeu (cliente-service na 8081 ou "
                                + "cotacao-service na 8082). Confira se ambos estao no ar."));
    }

    /** Vizinho respondeu, mas com erro dele (5xx). */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErroResposta> tratarErroDoVizinho(HttpServerErrorException excecao) {
        LOG.error("Vizinho respondeu com erro", excecao);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErroResposta.de(HttpStatus.SERVICE_UNAVAILABLE.value(), "Servico indisponivel",
                        "Um servico do qual dependemos falhou (" + excecao.getStatusCode() + ")."));
    }
}
