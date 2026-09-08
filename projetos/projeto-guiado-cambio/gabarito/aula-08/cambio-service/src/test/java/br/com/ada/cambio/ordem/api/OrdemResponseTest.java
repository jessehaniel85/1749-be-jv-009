package br.com.ada.cambio.ordem.api;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ada.cambio.ordem.dominio.Moeda;
import br.com.ada.cambio.ordem.dominio.OrdemDeCompra;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** BUILDER: cada valor entra com nome, e a ordem das chamadas nao importa. */
class OrdemResponseTest {

    @Test
    @DisplayName("o builder monta o mesmo objeto, em qualquer ordem de chamada")
    void ordemDasChamadasNaoImporta() {
        OrdemResponse umaOrdem = OrdemResponse.construtor()
                .idCompra(1L)
                .idCliente(2L)
                .cpfCliente("43488428095")
                .dataSolicitacao(LocalDateTime.of(2026, 9, 14, 16, 11, 23))
                .tipoMoeda(Moeda.EUR)
                .valorMoedaEstrangeira(new BigDecimal("100.00"))
                .valorCotacao(new BigDecimal("6.5857"))
                .valorTotalOperacao(new BigDecimal("661.86"))
                .numeroAgenciaRetirada("7057")
                .construir();

        OrdemResponse outraOrdem = OrdemResponse.construtor()
                .numeroAgenciaRetirada("7057")
                .valorTotalOperacao(new BigDecimal("661.86"))
                .valorCotacao(new BigDecimal("6.5857"))
                .valorMoedaEstrangeira(new BigDecimal("100.00"))
                .tipoMoeda(Moeda.EUR)
                .dataSolicitacao(LocalDateTime.of(2026, 9, 14, 16, 11, 23))
                .cpfCliente("43488428095")
                .idCliente(2L)
                .idCompra(1L)
                .construir();

        assertThat(umaOrdem).isEqualTo(outraOrdem);
    }

    @Test
    @DisplayName("de(OrdemDeCompra) preenche todos os campos do comprovante")
    void converteEntidade() {
        OrdemDeCompra ordem = new OrdemDeCompra(7L, "43488428095",
                LocalDateTime.of(2026, 9, 14, 16, 11, 23), Moeda.EUR,
                new BigDecimal("100.00"), new BigDecimal("6.5857"),
                new BigDecimal("661.86"), "7057");

        OrdemResponse resposta = OrdemResponse.de(ordem);

        assertThat(resposta.idCliente()).isEqualTo(7L);
        assertThat(resposta.cpfCliente()).isEqualTo("43488428095");
        assertThat(resposta.tipoMoeda()).isEqualTo(Moeda.EUR);
        assertThat(resposta.valorTotalOperacao()).isEqualByComparingTo("661.86");
        assertThat(resposta.numeroAgenciaRetirada()).isEqualTo("7057");
    }
}
