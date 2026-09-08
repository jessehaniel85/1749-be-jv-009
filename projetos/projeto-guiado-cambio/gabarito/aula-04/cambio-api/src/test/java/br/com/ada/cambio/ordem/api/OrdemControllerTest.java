package br.com.ada.cambio.ordem.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ada.cambio.cliente.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import br.com.ada.cambio.cotacao.dominio.MoedaNaoSuportadaException;
import br.com.ada.cambio.ordem.dominio.AgenciaInvalidaException;
import br.com.ada.cambio.ordem.dominio.OrdemDeCompra;
import br.com.ada.cambio.ordem.dominio.OrdemNaoEncontradaException;
import br.com.ada.cambio.ordem.dominio.OrdemService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Fatia web da ordem. O ponto central deste teste é o <b>contrato snake_case</b>: se alguém
 * renomear um campo do DTO sem atualizar o {@code @JsonProperty}, o app do cliente quebra —
 * e é aqui que isso aparece, não em produção.
 */
@WebMvcTest(OrdemController.class)
class OrdemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdemService servico;

    private static final String PEDIDO_VALIDO = """
            {
              "cpf": "43488428095",
              "moeda": "EUR",
              "valorMoedaEstrangeira": 100.0,
              "numeroAgenciaRetirada": "7057"
            }
            """;

    private OrdemDeCompra ordemRegistrada() {
        OrdemDeCompra ordem = new OrdemDeCompra(
                1L,
                "43488428095",
                LocalDateTime.of(2026, 9, 14, 16, 11, 23, 866_000_000),
                Moeda.EUR,
                new BigDecimal("100.0"),
                new BigDecimal("6.5857"),
                new BigDecimal("658.57"),
                "7057");
        ReflectionTestUtils.setField(ordem, "id", 1L);
        return ordem;
    }

    @Test
    @DisplayName("POST /ordens devolve 201 com o comprovante no contrato snake_case do módulo")
    void registroValidoDevolve201() throws Exception {
        when(servico.registrar(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(ordemRegistrada());

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(PEDIDO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id_compra").value(1))
                .andExpect(jsonPath("$.id_cliente").value(1))
                .andExpect(jsonPath("$.cpf_cliente").value("43488428095"))
                .andExpect(jsonPath("$.dataSolicitacao").value("2026-09-14T16:11:23.866"))
                .andExpect(jsonPath("$.tipo_moeda").value("EUR"))
                .andExpect(jsonPath("$.valor_moeda_estrangeira").value(100.0))
                .andExpect(jsonPath("$.valor_cotacao").value(6.5857))
                .andExpect(jsonPath("$.valor_total_operacao").value(658.57))
                .andExpect(jsonPath("$.numero_agencia_retirada").value("7057"));
    }

    @Test
    @DisplayName("POST /ordens sem valor devolve 400 (validação de entrada)")
    void pedidoSemValorDevolve400() throws Exception {
        String semValor = """
                {"cpf": "43488428095", "moeda": "EUR", "numeroAgenciaRetirada": "7057"}
                """;

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(semValor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("valorMoedaEstrangeira"));
    }

    @Test
    @DisplayName("POST /ordens com CPF sem cadastro devolve 404")
    void clienteInexistenteDevolve404() throws Exception {
        when(servico.registrar(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenThrow(new ClienteNaoEncontradoException("43488428095"));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(PEDIDO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /ordens com moeda não operada devolve 422")
    void moedaNaoSuportadaDevolve422() throws Exception {
        when(servico.registrar(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenThrow(new MoedaNaoSuportadaException("JPY"));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(PEDIDO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("POST /ordens com agência fora do formato devolve 422")
    void agenciaInvalidaDevolve422() throws Exception {
        when(servico.registrar(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenThrow(new AgenciaInvalidaException("705"));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(PEDIDO_VALIDO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /ordens/{id} existente devolve 200")
    void consultaExistenteDevolve200() throws Exception {
        when(servico.buscarPorId(1L)).thenReturn(ordemRegistrada());

        mockMvc.perform(get("/ordens/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_compra").value(1))
                .andExpect(jsonPath("$.valor_total_operacao").value(658.57));
    }

    @Test
    @DisplayName("GET /ordens/{id} inexistente devolve 404")
    void consultaInexistenteDevolve404() throws Exception {
        when(servico.buscarPorId(99L)).thenThrow(new OrdemNaoEncontradaException(99L));

        mockMvc.perform(get("/ordens/99"))
                .andExpect(status().isNotFound());
    }
}
