package br.com.ada.cambio.ordem.dominio;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Regra comercial do DOLAR: conversao direta, <b>sem spread</b>.
 *
 * <p>O dolar e a moeda de maior volume da mesa; a instituicao repassa a cotacao
 * cheia e ganha no giro. Regra de negocio, nao detalhe tecnico — por isso esta
 * escrita num arquivo com nome proprio, e nao num {@code else} de um switch.</p>
 */
@Component
public class CalculoUsdStrategy implements CalculoOperacaoStrategy {

    @Override
    public Moeda moeda() {
        return Moeda.USD;
    }

    @Override
    public BigDecimal calcular(BigDecimal valorMoedaEstrangeira, BigDecimal cotacao) {
        return valorMoedaEstrangeira
                .multiply(cotacao)
                .setScale(RegrasDeCambio.ESCALA_MONETARIA, RegrasDeCambio.ARREDONDAMENTO);
    }
}
