package br.com.ada.cambio.cotacao.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoService;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CotacaoController.class)
class CotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CotacaoService cotacaoService;

    @Test
    @DisplayName("GET /cotacoes/USD devolve 200 com valor e dataHora")
    void consultaCotacao() throws Exception {
        when(cotacaoService.consultar(Moeda.USD))
                .thenReturn(new Cotacao(Moeda.USD, new BigDecimal("5.4321"), LocalDateTime.now()));

        mockMvc.perform(get("/cotacoes/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moeda").value("USD"))
                .andExpect(jsonPath("$.valorCotacao").value(5.4321));
    }

    @Test
    @DisplayName("GET /cotacoes/JPY devolve 422")
    void moedaNaoSuportada() throws Exception {
        mockMvc.perform(get("/cotacoes/JPY"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("PUT /cotacoes/EUR atualiza o valor")
    void atualizaCotacao() throws Exception {
        when(cotacaoService.atualizar(ArgumentMatchers.eq(Moeda.EUR), ArgumentMatchers.any()))
                .thenReturn(new Cotacao(Moeda.EUR, new BigDecimal("7.1234"), LocalDateTime.now()));

        mockMvc.perform(put("/cotacoes/EUR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AtualizacaoCotacaoRequest(new BigDecimal("7.1234")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorCotacao").value(7.1234));
    }

    @Test
    @DisplayName("PUT /cotacoes/EUR com valor negativo devolve 400")
    void recusaValorInvalido() throws Exception {
        mockMvc.perform(put("/cotacoes/EUR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AtualizacaoCotacaoRequest(new BigDecimal("-1")))))
                .andExpect(status().isBadRequest());
    }
}
