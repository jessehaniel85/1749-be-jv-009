package br.com.ada.cambio.cliente.dominio;

/**
 * Porta de LEITURA do dominio Cliente (ISP).
 *
 * <p>Quem so precisa consultar depende desta interface e nao enxerga
 * {@code cadastrar}. Interface pequena e interface que nao obriga o cliente a
 * conhecer o que ele nao usa.</p>
 */
public interface ConsultaCliente {

    /**
     * Busca um cliente pelo CPF.
     *
     * <p><b>Pre-condicao:</b> {@code cpf} nao nulo, com
     * {@link RegrasDeCliente#TAMANHO_CPF} digitos.<br>
     * <b>Pos-condicao:</b> devolve um cliente com {@code id} preenchido, ou
     * estoura {@link ClienteNaoEncontradoException}. Nunca devolve {@code null}.</p>
     */
    Cliente buscarPorCpf(String cpf);
}
