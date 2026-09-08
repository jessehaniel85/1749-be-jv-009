package br.com.ada.cambio.ordem.dominio;

import org.springframework.stereotype.Component;

/**
 * SRP: uma unica razao para mudar — as regras de aceitacao de um pedido.
 *
 * <p>Na Aula 8 ele passou a receber o {@link CatalogoDeMoedas} por construtor,
 * em vez de chamar {@code Moeda.deSigla} direto. Ganho: o catalogo virou uma
 * dependencia visivel e substituivel (um catalogo lido do banco entra sem tocar
 * aqui).</p>
 */
@Component
public class ValidadorDeOrdem {

    private final CatalogoDeMoedas catalogo;

    public ValidadorDeOrdem(CatalogoDeMoedas catalogo) {
        this.catalogo = catalogo;
    }

    /**
     * Traduz a sigla recebida em uma {@link Moeda} do catalogo.
     *
     * @throws MoedaNaoSuportadaException se a sigla nao estiver no catalogo
     */
    public Moeda moedaDe(String sigla) {
        return catalogo.resolver(sigla);
    }

    /**
     * Confere o formato do numero da agencia de retirada.
     *
     * @throws AgenciaInvalidaException se nao forem exatamente
     *         {@link RegrasDeCambio#TAMANHO_AGENCIA} digitos
     */
    public void validarAgencia(String numeroAgenciaRetirada) {
        if (!temFormatoDeAgencia(numeroAgenciaRetirada)) {
            throw new AgenciaInvalidaException(numeroAgenciaRetirada);
        }
    }

    private boolean temFormatoDeAgencia(String numeroAgencia) {
        return numeroAgencia != null && numeroAgencia.matches(RegrasDeCambio.PADRAO_AGENCIA);
    }
}
