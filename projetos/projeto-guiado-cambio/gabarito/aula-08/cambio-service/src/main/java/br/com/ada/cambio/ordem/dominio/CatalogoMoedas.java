package br.com.ada.cambio.ordem.dominio;

import java.util.EnumSet;
import java.util.Set;

/**
 * SINGLETON CLASSICO — material didatico da Aula 8. <b>Nao e usado no fluxo
 * real</b>; quem roda em producao e {@link CatalogoMoedasBean}.
 *
 * <p>Esta e a forma que aparece no livro do GoF e em quase todo codigo legado
 * Java. Vale escrever a mao uma vez para entender as armadilhas:</p>
 *
 * <ul>
 *   <li><b>Construtor privado</b> — ninguem consegue dar {@code new};</li>
 *   <li><b>Holder idiom</b> — a classe interna so e carregada na primeira chamada
 *       a {@code getInstance()}, e a JVM garante que a inicializacao de uma classe
 *       acontece uma unica vez, com seguranca de thread. E o jeito correto de
 *       fazer inicializacao preguicosa <b>sem</b> {@code synchronized} e sem o
 *       famigerado double-checked locking, que era quebrado antes do Java 5;</li>
 *   <li><b>Ainda assim tem defeitos:</b> nao da para substituir em teste, carrega
 *       estado global pelo ClassLoader e pode ser burlado por reflexao
 *       ({@code setAccessible(true)}) ou por desserializacao.</li>
 * </ul>
 */
public final class CatalogoMoedas implements CatalogoDeMoedas {

    private final Set<Moeda> moedas = EnumSet.allOf(Moeda.class);

    private CatalogoMoedas() {
        // construtor privado: a unica porta de entrada e getInstance()
    }

    /**
     * Holder idiom: {@code Suporte} so e carregada quando {@code getInstance()}
     * e chamado pela primeira vez. Preguicoso e seguro para threads de graca.
     */
    private static final class Suporte {
        private static final CatalogoMoedas INSTANCIA = new CatalogoMoedas();
    }

    public static CatalogoMoedas getInstance() {
        return Suporte.INSTANCIA;
    }

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
