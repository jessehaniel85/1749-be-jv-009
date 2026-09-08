package br.com.ada.cambio.ordem.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ada.cambio.ordem.dominio.AgenciaInvalidaException;
import br.com.ada.cambio.ordem.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.ordem.dominio.Moeda;
import br.com.ada.cambio.ordem.dominio.OrdemDeCompra;
import br.com.ada.cambio.ordem.dominio.OrdemService;
import br.com.ada.cambio.ordem.dominio.ServicoIndisponivelException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

@WebMvcTest(OrdemController.class)
class OrdemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrdemService ordemService;

    private OrdemDeCompra ordemExemplo() {
        return new OrdemDeCompra(1L, "43488428095", LocalDateTime.of(2026, 9, 14, 16, 11, 23),
                Moeda.EUR, new BigDecimal("100.00"), new BigDecimal("6.5857"),
                new BigDecimal("658.57"), "7057");
    }

    private String corpoValido() throws Exception {
        return objectMapper.writeValueAsString(new OrdemRequest(
                "43488428095", "EUR", new BigDecimal("100.00"), "7057"));
    }

    @Test
    @DisplayName("POST /ordens devolve 201 no contrato snake_case do modulo")
    void registraOrdem() throws Exception {
        when(ordemService.registrar(anyString(), anyString(), any(), anyString()))
                .thenReturn(ordemExemplo());

        mockMvc.perform(post("/ordens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf_cliente").value("43488428095"))
                .andExpect(jsonPath("$.tipo_moeda").value("EUR"))
                .andExpect(jsonPath("$.valor_cotacao").value(6.5857))
                .andExpect(jsonPath("$.valor_total_operacao").value(658.57))
                .andExpect(jsonPath("$.numero_agencia_retirada").value("7057"))
                .andExpect(jsonPath("$.dataSolicitacao").value("2026-09-14T16:11:23"));
    }

    @Test
    @DisplayName("POST /ordens com CPF fora do formato devolve 400")
    void cpfInvalido() throws Exception {
        String corpo = objectMapper.writeValueAsString(new OrdemRequest(
                "123", "EUR", new BigDecimal("100.00"), "7057"));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cliente inexistente devolve 404")
    void clienteInexistente() throws Exception {
        when(ordemService.registrar(anyString(), anyString(), any(), anyString()))
                .thenThrow(new ClienteNaoEncontradoException("43488428095"));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(corpoValido()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("agencia invalida devolve 422")
    void agenciaInvalida() throws Exception {
        when(ordemService.registrar(anyString(), anyString(), any(), anyString()))
                .thenThrow(new AgenciaInvalidaException("705"));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(corpoValido()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("vizinho fora do ar devolve 503 com mensagem clara")
    void vizinhoForaDoAr() throws Exception {
        when(ordemService.registrar(anyString(), anyString(), any(), anyString()))
                .thenThrow(new ResourceAccessException("Connection refused"));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(corpoValido()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    @DisplayName("client que detectou o vizinho fora do ar tambem devolve 503")
    void vizinhoForaDoArPeloClient() throws Exception {
        when(ordemService.registrar(anyString(), anyString(), any(), anyString()))
                .thenThrow(new ServicoIndisponivelException("cliente-service (8081)",
                        new ResourceAccessException("Connection refused")));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(corpoValido()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("cliente-service")));
    }

    @Test
    @DisplayName("GET /ordens/{id} devolve 200")
    void buscaOrdem() throws Exception {
        when(ordemService.buscarPorId(1L)).thenReturn(ordemExemplo());

        mockMvc.perform(get("/ordens/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_cliente").value(1));
    }
}
