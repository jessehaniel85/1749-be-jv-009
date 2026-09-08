package br.com.ada.cambio.ordem.dominio;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Ponto unico de calculo do valor da operacao.
 *
 * <p>Na Aula 7 esta classe TINHA a formula. Na Aula 8 ela apenas <b>escolhe
 * quem sabe a formula</b> e delega — e o corpo do metodo deixou de crescer
 * quando o negocio cria uma regra por moeda.</p>
 *
 * <p>Repare que nao ha {@code switch} nem {@code if} de moeda em lugar nenhum:
 * a escolha e uma busca em mapa. Esse e o cheiro de um Strategy bem colocado.</p>
 */
@Component
public class CalculadoraDeOperacao {

    private final Map<Moeda, CalculoOperacaoStrategy> estrategiasPorMoeda;

    public CalculadoraDeOperacao(Map<Moeda, CalculoOperacaoStrategy> estrategiasPorMoeda) {
        this.estrategiasPorMoeda = estrategiasPorMoeda;
    }

    /**
     * Converte o valor em moeda estrangeira para reais usando a regra comercial
     * da moeda.
     *
     * @throws MoedaNaoSuportadaException se nao houver estrategia para a moeda
     */
    public BigDecimal calcularTotal(Moeda moeda,
                                    BigDecimal valorMoedaEstrangeira,
                                    BigDecimal valorCotacao) {
        return estrategiaDe(moeda).calcular(valorMoedaEstrangeira, valorCotacao);
    }

    private CalculoOperacaoStrategy estrategiaDe(Moeda moeda) {
        CalculoOperacaoStrategy estrategia = estrategiasPorMoeda.get(moeda);
        if (estrategia == null) {
            throw new MoedaNaoSuportadaException(moeda.name());
        }
        return estrategia;
    }
}
