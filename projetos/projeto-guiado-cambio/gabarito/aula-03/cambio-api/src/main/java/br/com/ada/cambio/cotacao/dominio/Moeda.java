package br.com.ada.cambio.cotacao.dominio;

/**
 * Moedas estrangeiras operadas pela mesa de câmbio.
 *
 * <p>A conversão de sigla para enum é feita aqui, e não pelo Spring no path variable,
 * de propósito: se deixássemos o Spring converter, uma sigla inexistente viraria
 * {@code 400} genérico antes de o domínio ser consultado. O contrato do módulo pede
 * {@code 422} — e quem decide isso é o domínio.</p>
 */
public enum Moeda {

    USD,
    EUR;

    /**
     * Converte a sigla textual na moeda correspondente (aceita minúsculas).
     *
     * @throws MoedaNaoSuportadaException se a sigla não for USD nem EUR
     */
    public static Moeda paraSigla(String sigla) {
        if (sigla == null || sigla.isBlank()) {
            throw new MoedaNaoSuportadaException(sigla);
        }
        for (Moeda moeda : values()) {
            if (moeda.name().equalsIgnoreCase(sigla.trim())) {
                return moeda;
            }
        }
        throw new MoedaNaoSuportadaException(sigla);
    }
}
