package br.com.ada.cambio.ordem.dominio;

/**
 * Nao existe cliente com o CPF informado. Vira HTTP 404.
 *
 * <p>Voltou na Aula 7. Na Aula 6 o {@code TratadorDeErros} lidava direto com
 * {@code FeignException.NotFound} — infraestrutura vazando ate a borda. Agora o
 * Adapter traduz na fronteira e o resto do sistema so conhece dominio.</p>
 */
public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(String cpf) {
        super("Cliente nao encontrado para o CPF " + cpf);
    }
}
