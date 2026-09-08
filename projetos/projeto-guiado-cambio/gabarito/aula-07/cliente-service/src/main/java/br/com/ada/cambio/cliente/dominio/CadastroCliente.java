package br.com.ada.cambio.cliente.dominio;

/**
 * Porta de ESCRITA do dominio Cliente (ISP).
 *
 * <p>Separada de {@link ConsultaCliente} de proposito: sao dois motivos
 * diferentes para a interface mudar, e dois publicos diferentes.</p>
 */
public interface CadastroCliente {

    /**
     * Cadastra um cliente novo.
     *
     * <p><b>Pre-condicao:</b> cliente valido e ainda sem {@code id}.<br>
     * <b>Pos-condicao:</b> devolve o cliente persistido com {@code id}
     * preenchido, ou estoura {@link CpfDuplicadoException} se o CPF ja existir.</p>
     */
    Cliente cadastrar(Cliente cliente);
}
