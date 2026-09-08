package br.com.ada.cambio.ordem.dominio;

import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * A implementacao que o FLUXO REAL usa.
 *
 * <p>Nao tem {@code getInstance()}, nao tem campo estatico e nao tem
 * {@code synchronized} — e mesmo assim existe <b>uma unica instancia</b> na
 * aplicacao, porque o escopo padrao de bean do Spring ja e singleton.</p>
 *
 * <p>Vantagens sobre o Singleton classico, e sao praticas:</p>
 * <ul>
 *   <li><b>Testavel:</b> quem usa recebe {@link CatalogoDeMoedas} por construtor
 *       e o teste passa um dublê. Com {@code getInstance()} chamado la dentro,
 *       nao haveria como substituir;</li>
 *   <li><b>Substituivel:</b> um catalogo lido do banco entra como outro bean;</li>
 *   <li><b>Sem estado global:</b> o ciclo de vida e do contexto, nao do
 *       ClassLoader — dois testes nao contaminam um ao outro.</li>
 * </ul>
 */
@Component
public class CatalogoMoedasBean implements CatalogoDeMoedas {

    private static final Set<Moeda> MOEDAS = EnumSet.allOf(Moeda.class);

    @Override
    public Set<Moeda> disponiveis() {
        return Set.copyOf(MOEDAS);
    }

    @Override
    public boolean suporta(String sigla) {
        return Moeda.deSigla(sigla).isPresent();
    }

    @Override
    public Moeda resolver(String sigla) {
        return Moeda.deSigla(sigla)
                .orElseThrow(() -> new MoedaNaoSuportadaException(sigla));
    }
}
