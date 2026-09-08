package br.com.ada.cambio.ordem.infra;

import br.com.ada.cambio.ordem.dominio.CalculoOperacaoStrategy;
import br.com.ada.cambio.ordem.dominio.Moeda;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Monta o registro {@code Map<Moeda, CalculoOperacaoStrategy>} a partir de TODAS
 * as estrategias que o Spring encontrar no contexto.
 *
 * <p>Este e o encaixe que torna o Strategy indolor: o Spring injeta a
 * {@code List} de implementacoes (ele sabe descobrir todos os beans de um tipo),
 * e aqui viram um mapa indexado pela moeda. Uma moeda nova precisa de
 * <b>um arquivo</b>; este arquivo nao muda.</p>
 *
 * <p>Fica em {@code infra} porque e fiacao do framework, nao regra de negocio.
 * As estrategias em si vivem no {@code dominio}.</p>
 */
@Configuration
public class EstrategiasDeCalculoConfig {

    /**
     * @throws IllegalStateException se duas estrategias reivindicarem a mesma
     *         moeda — melhor falhar na subida do que sortear qual vale.
     */
    @Bean
    public Map<Moeda, CalculoOperacaoStrategy> estrategiasPorMoeda(
            List<CalculoOperacaoStrategy> estrategias) {

        Map<Moeda, CalculoOperacaoStrategy> registro = estrategias.stream()
                .collect(Collectors.toMap(
                        CalculoOperacaoStrategy::moeda,
                        Function.identity(),
                        (primeira, segunda) -> {
                            throw new IllegalStateException(
                                    "Duas estrategias para a mesma moeda: "
                                            + primeira.getClass().getSimpleName() + " e "
                                            + segunda.getClass().getSimpleName());
                        }));

        // Rede de seguranca: moeda no catalogo sem estrategia so apareceria em
        // producao, na primeira ordem daquela moeda. Aqui aparece na subida.
        for (Moeda moeda : Moeda.values()) {
            if (!registro.containsKey(moeda)) {
                throw new IllegalStateException(
                        "Nenhuma CalculoOperacaoStrategy registrada para " + moeda);
            }
        }
        return Map.copyOf(registro);
    }
}
