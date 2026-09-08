package br.com.ada.cambio.ordem.dominio;

import java.util.EnumSet;
import java.util.Set;

/**
 * SINGLETON BASEADO EM ENUM — material didatico da Aula 8. <b>Nao e usado no
 * fluxo real</b>.
 *
 * <p>Joshua Bloch, no <i>Effective Java</i>, chama esta de "a melhor forma de
 * implementar um singleton". Em quatro linhas ela resolve o que o
 * {@link CatalogoMoedas} nao resolve:</p>
 *
 * <ul>
 *   <li><b>a prova de reflexao</b> — a JVM proibe instanciar enum por reflexao;</li>
 *   <li><b>a prova de serializacao</b> — enum tem serializacao especial, nao
 *       cria copia ao desserializar;</li>
 *   <li><b>seguro para threads</b> por construcao, sem escrever nada.</li>
 * </ul>
 *
 * <p>Continua com o defeito que importa para nos: <b>estado global e
 * insubstituivel em teste</b>. Por isso o fluxo real usa o bean.</p>
 */
public enum CatalogoMoedasEnum implements CatalogoDeMoedas {

    INSTANCIA;

    private final Set<Moeda> moedas = EnumSet.allOf(Moeda.class);

    @Override
    public Set<Moeda> disponiveis() {
        return Set.copyOf(moedas);
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
