package br.com.ada.cambio.cliente.dominio;

/**
 * Constantes do dominio Cliente.
 *
 * <p>Antes da Aula 7, o numero 11 aparecia em tres lugares: no
 * {@code @Pattern} do DTO, no {@code length} da coluna e na mensagem de erro.
 * Mudar a regra exigia lembrar dos tres. Agora existe UM lugar.</p>
 *
 * <p>{@code PADRAO_CPF} e uma expressao constante em tempo de compilacao
 * (concatenacao de constantes), por isso pode ser usada dentro de anotacoes.</p>
 */
public final class RegrasDeCliente {

    /** Um CPF tem 11 digitos. Nao e "11", e "o tamanho do CPF". */
    public static final int TAMANHO_CPF = 11;

    public static final String PADRAO_CPF = "\\d{" + TAMANHO_CPF + "}";

    public static final int TAMANHO_MAXIMO_NOME = 120;

    private RegrasDeCliente() {
        // classe de constantes nao se instancia
    }
}
