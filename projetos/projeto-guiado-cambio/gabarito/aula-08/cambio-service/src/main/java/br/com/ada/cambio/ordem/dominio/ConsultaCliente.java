package br.com.ada.cambio.ordem.dominio;

/**
 * Porta de saida para consultar clientes (ISP + DIP).
 *
 * <p><b>ISP:</b> o cambio-service <b>so consulta</b> clientes. Nao cadastra,
 * nao altera, nao remove. Por isso a porta tem exatamente um metodo. Nao existe
 * uma {@code CadastroCliente} aqui — e a ausencia dela que prova o ponto: no
 * cliente-service as duas portas existem, porque la os dois usos existem.</p>
 *
 * <p><b>DIP:</b> esta interface pertence ao <b>dominio</b>. Quem a implementa
 * ({@code ClienteClientAdapter}, em {@code infra}) e que depende dela, e nao o
 * contrario. A seta de dependencia aponta para dentro.</p>
 *
 * <p><b>Pre-condicao:</b> {@code cpf} nao nulo, com
 * {@link RegrasDeCambio#TAMANHO_CPF} digitos.<br>
 * <b>Pos-condicao:</b> devolve um {@link ClienteEncontrado} com {@code id}
 * preenchido; estoura {@link ClienteNaoEncontradoException} se nao existir e
 * {@link ServicoIndisponivelException} se a fonte nao responder. Nunca devolve
 * {@code null}.</p>
 */
public interface ConsultaCliente {

    ClienteEncontrado porCpf(String cpf);
}
