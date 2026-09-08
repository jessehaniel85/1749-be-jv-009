package br.com.ada.cambio.cliente.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.cliente.dominio.ClienteService;
import br.com.ada.cambio.cliente.dominio.CpfDuplicadoException;
import br.com.ada.cambio.cliente.dominio.EstadoCivil;
import br.com.ada.cambio.cliente.dominio.Sexo;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MEIO da pirâmide: teste de fatia web.
 *
 * <p>{@code @WebMvcTest} sobe só a camada MVC (controller, conversão JSON, validação e o
 * {@code @RestControllerAdvice}) — sem banco, sem repositório. O serviço é substituído por
 * um mock com {@code @MockitoBean}. O que se testa aqui é o <b>contrato HTTP</b>:
 * status, header e formato do corpo.</p>
 */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService servico;

    private static final String JSON_VALIDO = """
            {
              "nome": "Marina Alcântara",
              "cpf": "43488428095",
              "dataNascimento": "1991-04-17",
              "estadoCivil": "SOLTEIRO",
              "sexo": "FEMININO"
            }
            """;

    @Test
    @DisplayName("POST /clientes com corpo válido devolve 201 e Location")
    void cadastroValidoDevolve201() throws Exception {
        Cliente salvo = new Cliente("Marina Alcântara", "43488428095",
                LocalDate.of(1991, 4, 17), EstadoCivil.SOLTEIRO, Sexo.FEMININO);
        when(servico.cadastrar(any(Cliente.class))).thenReturn(salvo);

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/clientes/43488428095"))
                .andExpect(jsonPath("$.cpf").value("43488428095"))
                .andExpect(jsonPath("$.nome").value("Marina Alcântara"));
    }

    @Test
    @DisplayName("POST /clientes com CPF fora do formato devolve 400 e lista os campos")
    void cadastroInvalidoDevolve400() throws Exception {
        String corpoInvalido = """
                {
                  "nome": "",
                  "cpf": "434.884.280-95",
                  "dataNascimento": "1991-04-17",
                  "estadoCivil": "SOLTEIRO",
                  "sexo": "FEMININO"
                }
                """;

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros.length()").value(2));
    }

    @Test
    @DisplayName("POST /clientes com CPF já cadastrado devolve 409")
    void cadastroDuplicadoDevolve409() throws Exception {
        when(servico.cadastrar(any(Cliente.class))).thenThrow(new CpfDuplicadoException("43488428095"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /clientes/{cpf} existente devolve 200")
    void consultaExistenteDevolve200() throws Exception {
        when(servico.buscarPorCpf("43488428095")).thenReturn(new Cliente("Marina Alcântara",
                "43488428095", LocalDate.of(1991, 4, 17), EstadoCivil.SOLTEIRO, Sexo.FEMININO));

        mockMvc.perform(get("/clientes/43488428095"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoCivil").value("SOLTEIRO"));
    }

    @Test
    @DisplayName("GET /clientes/{cpf} inexistente devolve 404")
    void consultaInexistenteDevolve404() throws Exception {
        when(servico.buscarPorCpf("00000000000"))
                .thenThrow(new ClienteNaoEncontradoException("00000000000"));

        mockMvc.perform(get("/clientes/00000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
