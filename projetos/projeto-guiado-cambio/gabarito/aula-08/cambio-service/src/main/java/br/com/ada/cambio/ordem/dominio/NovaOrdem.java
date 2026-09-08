package br.com.ada.cambio.ordem.dominio;

import java.math.BigDecimal;

/**
 * Pedido de ordem, na linguagem do dominio.
 *
 * <p>Antes: {@code registrar(String, String, BigDecimal, String)} — quatro
 * parametros, dois deles String, na ordem que so o autor lembra. Trocar
 * {@code cpfCliente} por {@code numeroAgenciaRetirada} compilava e quebrava em
 * producao.</p>
 *
 * <p>Agora e um objeto com nomes. E o dominio nao precisa importar o DTO da
 * camada {@code api} para receber os dados.</p>
 */
public record NovaOrdem(String cpfCliente,
                        String siglaMoeda,
                        BigDecimal valorMoedaEstrangeira,
                        String numeroAgenciaRetirada) {
}
