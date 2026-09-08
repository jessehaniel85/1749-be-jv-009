package br.com.ada.cambio.ordem.dominio;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * SRP: uma unica razao para mudar — <b>como se calcula o valor da operacao</b>.
 *
 * <p>Isolada, esta regra fica trivial de testar (entra valor e cotacao, sai
 * total) e trivial de auditar: quem quiser conferir o arredondamento abre um
 * arquivo de dez linhas, nao um service de cem.</p>
 *
 * <p>Na Aula 8 esta classe vira o ponto de extensao do <b>Strategy</b>: cada
 * moeda com a sua regra. Repare que isso so ficou facil PORQUE o calculo foi
 * separado aqui primeiro.</p>
 */
@Component
public class CalculadoraDeOperacao {

    /**
     * Converte o valor em moeda estrangeira para reais.
     *
     * <p><b>Pos-condicao:</b> devolve o total com
     * {@link RegrasDeCambio#ESCALA_MONETARIA} casas, arredondado por
     * {@link RegrasDeCambio#ARREDONDAMENTO}.</p>
     */
    public BigDecimal calcularTotal(BigDecimal valorMoedaEstrangeira, BigDecimal valorCotacao) {
        return valorMoedaEstrangeira
                .multiply(valorCotacao)
                .setScale(RegrasDeCambio.ESCALA_MONETARIA, RegrasDeCambio.ARREDONDAMENTO);
    }
}
