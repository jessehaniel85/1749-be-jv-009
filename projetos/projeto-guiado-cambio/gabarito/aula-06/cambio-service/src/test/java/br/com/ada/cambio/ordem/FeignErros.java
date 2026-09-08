package br.com.ada.cambio.ordem;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Fabrica de erros do Feign para os testes.
 *
 * <p>Construir uma {@code FeignException} a mao e chato - por isso ela vive aqui,
 * fora dos testes que a usam. Repare no incomodo didatico: para testar o nosso
 * codigo precisamos instanciar tipos da BIBLIOTECA. Isso e sintoma de acoplamento
 * a infraestrutura, e a Aula 7 conserta.</p>
 */
public final class FeignErros {

    private FeignErros() {
    }

    private static Request requisicaoFalsa(String caminho) {
        return Request.create(Request.HttpMethod.GET, caminho, Map.of(), null,
                StandardCharsets.UTF_8, null);
    }

    /** O vizinho respondeu 404. */
    public static FeignException.NotFound naoEncontrado(String caminho) {
        return new FeignException.NotFound("nao encontrado", requisicaoFalsa(caminho),
                null, Map.of());
    }

    /** O vizinho respondeu 422. */
    public static FeignException.UnprocessableEntity naoProcessavel(String caminho) {
        return new FeignException.UnprocessableEntity("nao processavel", requisicaoFalsa(caminho),
                null, Map.of());
    }

    /** O vizinho nem atendeu: conexao recusada ou timeout. */
    public static RetryableException foraDoAr(String caminho) {
        return new RetryableException(-1, "Connection refused", Request.HttpMethod.GET,
                (Long) null, requisicaoFalsa(caminho));
    }

    /** O LoadBalancer nao achou instancia registrada no Eureka. */
    public static IllegalStateException semInstanciaRegistrada(String servico) {
        return new IllegalStateException("No instances available for " + servico);
    }
}
