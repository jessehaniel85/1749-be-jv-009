package br.com.ada.cambio.cliente.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.cliente.dominio.ClienteService;
import br.com.ada.cambio.cliente.dominio.CpfDuplicadoException;
import br.com.ada.cambio.cliente.dominio.EstadoCivil;
import br.com.ada.cambio.cliente.dominio.Sexo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Meio da piramide: fatia web, service mockado. */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    private Cliente clienteExemplo() {
        return new Cliente("Ana Souza", "43488428095", LocalDate.of(1990, 5, 10),
                EstadoCivil.SOLTEIRO, Sexo.FEMININO);
    }

    @Test
    @DisplayName("POST /clientes devolve 201 com o corpo do cliente")
    void cadastraCliente() throws Exception {
        when(clienteService.cadastrar(any())).thenReturn(clienteExemplo());
        ClienteRequest requisicao = new ClienteRequest("Ana Souza", "43488428095",
                LocalDate.of(1990, 5, 10), EstadoCivil.SOLTEIRO, Sexo.FEMININO);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("43488428095"))
                .andExpect(jsonPath("$.nome").value("Ana Souza"));
    }

    @Test
    @DisplayName("POST /clientes com CPF invalido devolve 400")
    void recusaCpfInvalido() throws Exception {
        ClienteRequest requisicao = new ClienteRequest("Ana Souza", "123",
                LocalDate.of(1990, 5, 10), EstadoCivil.SOLTEIRO, Sexo.FEMININO);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /clientes com CPF repetido devolve 409")
    void recusaCpfDuplicado() throws Exception {
        when(clienteService.cadastrar(any())).thenThrow(new CpfDuplicadoException("43488428095"));
        ClienteRequest requisicao = new ClienteRequest("Ana Souza", "43488428095",
                LocalDate.of(1990, 5, 10), EstadoCivil.SOLTEIRO, Sexo.FEMININO);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /clientes/{cpf} devolve 200")
    void buscaCliente() throws Exception {
        when(clienteService.buscarPorCpf("43488428095")).thenReturn(clienteExemplo());

        mockMvc.perform(get("/clientes/43488428095"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoCivil").value("SOLTEIRO"));
    }

    @Test
    @DisplayName("GET /clientes/{cpf} inexistente devolve 404")
    void clienteInexistente() throws Exception {
        when(clienteService.buscarPorCpf("00000000000"))
                .thenThrow(new ClienteNaoEncontradoException("00000000000"));

        mockMvc.perform(get("/clientes/00000000000"))
                .andExpect(status().isNotFound());
    }
}
