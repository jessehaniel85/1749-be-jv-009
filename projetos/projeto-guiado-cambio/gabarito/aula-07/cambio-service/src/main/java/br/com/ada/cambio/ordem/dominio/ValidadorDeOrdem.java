package br.com.ada.cambio.ordem.dominio;

import org.springframework.stereotype.Component;

/**
 * SRP: uma unica razao para mudar — <b>as regras de aceitacao de um pedido</b>.
 *
 * <p>Antes da Aula 7 estas duas verificacoes moravam dentro de
 * {@code OrdemService.registrar}, misturadas com chamada de rede, calculo e
 * persistencia. Uma mudanca na regra da agencia obrigava a abrir a classe que
 * tambem sabe salvar no banco.</p>
 *
 * <p>Nao precisa de nenhuma dependencia: e logica pura, e por isso o teste dela
 * roda em milissegundos e sem Spring.</p>
 */
@Component
public class ValidadorDeOrdem {

    /**
     * Traduz a sigla recebida em uma {@link Moeda} do catalogo.
     *
     * @throws MoedaNaoSuportadaException se a sigla nao estiver no catalogo
     */
    public Moeda moedaDe(String sigla) {
        return Moeda.deSigla(sigla)
                .orElseThrow(() -> new MoedaNaoSuportadaException(sigla));
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
