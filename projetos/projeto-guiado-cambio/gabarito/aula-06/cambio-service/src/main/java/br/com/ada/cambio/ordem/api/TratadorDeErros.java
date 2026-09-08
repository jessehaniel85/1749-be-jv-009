package br.com.ada.cambio.ordem.api;

import br.com.ada.cambio.ordem.dominio.AgenciaInvalidaException;
import br.com.ada.cambio.ordem.dominio.MoedaNaoSuportadaException;
import br.com.ada.cambio.ordem.dominio.OrdemNaoEncontradaException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import feign.RetryableException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz excecoes em status HTTP.
 *
 * <p>400 validacao - 404 nao encontrado - 422 moeda/agencia - 503 vizinho fora do ar.</p>
 *
 * <p><b>O incomodo da Aula 6:</b> ao trocar o {@code RestClient} pelo Feign,
 * o erro do vizinho passou a chegar aqui como {@code FeignException} - um tipo
 * de INFRAESTRUTURA vazando ate a borda da aplicacao. Funciona, mas o
 * {@code @RestControllerAdvice} agora precisa importar {@code feign.*}.
 * Guarde este import: e ele que a Aula 7 elimina com DIP + Adapter.</p>
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

    /** O vizinho respondeu 404: o CPF nao existe no cliente-service. */
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontradoNoVizinho(FeignException.NotFound excecao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResposta.de(HttpStatus.NOT_FOUND.value(), "Cliente nao encontrado",
                        "O cliente-service nao encontrou o CPF informado."));
    }

    /** O vizinho respondeu 422: moeda fora do catalogo dele. */
    @ExceptionHandler(FeignException.UnprocessableEntity.class)
    public ResponseEntity<ErroResposta> tratarRecusaDoVizinho(FeignException.UnprocessableEntity excecao) {
        return ResponseEntity.unprocessableEntity()
                .body(ErroResposta.de(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Moeda nao suportada",
                        "O cotacao-service nao trabalha com a moeda informada."));
    }

    /** Conexao recusada, timeout ou erro 5xx do vizinho. */
    @ExceptionHandler({RetryableException.class, FeignException.class})
    public ResponseEntity<ErroResposta> tratarVizinhoForaDoAr(FeignException excecao) {
        // 503 devolvido ao usuario, causa registrada no log: o operador precisa saber
        // QUAL vizinho falhou; o usuario, nao.
        LOG.error("Falha ao chamar servico vizinho", excecao);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErroResposta.de(HttpStatus.SERVICE_UNAVAILABLE.value(), "Servico indisponivel",
                        "Um servico do qual dependemos nao respondeu. Confira no painel do Eureka "
                                + "(http://localhost:8761) se cliente-service e cotacao-service estao registrados."));
    }

    /**
     * Caso especifico do LoadBalancer: o Eureka esta no ar, mas nenhuma instancia
     * do servico procurado esta registrada. Repare que a mensagem e DIFERENTE da
     * anterior - "ninguem atendeu" e "nem existe telefone" sao problemas distintos.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResposta> tratarSemInstancia(IllegalStateException excecao) {
        String mensagem = excecao.getMessage() == null ? "" : excecao.getMessage();
        if (!mensagem.contains("No instances available")) {
            return ResponseEntity.internalServerError()
                    .body(ErroResposta.de(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro interno", mensagem));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErroResposta.de(HttpStatus.SERVICE_UNAVAILABLE.value(), "Servico indisponivel",
                        "Nenhuma instancia registrada no Eureka para o servico procurado. "
                                + "Suba o servico ou use o perfil 'plano-c' (URLs fixas)."));
    }
}
