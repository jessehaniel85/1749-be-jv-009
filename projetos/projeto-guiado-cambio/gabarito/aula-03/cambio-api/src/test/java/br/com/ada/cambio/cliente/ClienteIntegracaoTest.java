package br.com.ada.cambio.cliente;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * TOPO da pirâmide: teste de integração.
 *
 * <p>Sobe a aplicação inteira (controller + serviço + repositório + H2 de verdade) e
 * exercita o fluxo de ponta a ponta: cadastra e depois consulta. É o teste mais lento e
 * o mais valioso quando quebra — ele prova que as peças se encaixam.</p>
 *
 * <p>{@code @Transactional} faz o rollback ao fim de cada teste, deixando o banco limpo
 * para o próximo.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClienteIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("fluxo completo: POST /clientes e depois GET /clientes/{cpf}")
    void cadastraEConsulta() throws Exception {
        String corpo = """
                {
                  "nome": "Rogério Bastos",
                  "cpf": "12345678901",
                  "dataNascimento": "1988-11-30",
                  "estadoCivil": "CASADO",
                  "sexo": "MASCULINO"
                }
                """;

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(get("/clientes/12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rogério Bastos"))
                .andExpect(jsonPath("$.estadoCivil").value("CASADO"));
    }

    @Test
    @DisplayName("cadastrar o mesmo CPF duas vezes devolve 409 na segunda")
    void cpfDuplicadoNoFluxoReal() throws Exception {
        String corpo = """
                {
                  "nome": "Lívia Menezes",
                  "cpf": "98765432100",
                  "dataNascimento": "1995-06-05",
                  "estadoCivil": "SOLTEIRO",
                  "sexo": "FEMININO"
                }
                """;

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
    }
}
