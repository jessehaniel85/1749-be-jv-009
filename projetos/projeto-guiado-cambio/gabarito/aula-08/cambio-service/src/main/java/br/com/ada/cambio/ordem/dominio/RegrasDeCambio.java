package br.com.ada.cambio.ordem.dominio;

import java.math.RoundingMode;

/**
 * Constantes do dominio de cambio.
 *
 * <p>Antes da Aula 7 estes numeros estavam soltos no meio do codigo:
 * {@code matches("\\d{4}")}, {@code setScale(2, HALF_EVEN)}. Numero solto no
 * meio de uma expressao e um <b>numero magico</b>: quem le nao sabe se o 2 e
 * escala monetaria, indice de array ou quantidade de tentativas.</p>
 *
 * <p>As constantes de PADRAO sao expressoes constantes em tempo de compilacao,
 * por isso podem ser usadas dentro de anotacoes.</p>
 */
public final class RegrasDeCambio {

    /** Um CPF tem 11 digitos. */
    public static final int TAMANHO_CPF = 11;

    /** Uma agencia de retirada tem 4 digitos. */
    public static final int TAMANHO_AGENCIA = 4;

    /** Reais tem 2 casas decimais. */
    public static final int ESCALA_MONETARIA = 2;

    /** Cotacao tem 4 casas decimais. */
    public static final int ESCALA_COTACAO = 4;

    /**
     * Arredondamento bancario (HALF_EVEN): metade para o vizinho PAR.
     * Diferente do HALF_UP da escola, ele nao enviesa a soma de muitos
     * arredondamentos para cima — por isso e o padrao em sistemas financeiros.
     */
    public static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;

    public static final String PADRAO_CPF = "\\d{" + TAMANHO_CPF + "}";

    public static final String PADRAO_AGENCIA = "\\d{" + TAMANHO_AGENCIA + "}";

    private RegrasDeCambio() {
        // classe de constantes nao se instancia
    }
}
