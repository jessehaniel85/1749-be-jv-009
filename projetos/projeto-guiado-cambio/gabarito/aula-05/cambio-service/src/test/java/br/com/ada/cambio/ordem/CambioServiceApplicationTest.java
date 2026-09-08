package br.com.ada.cambio.ordem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sobe o contexto do cambio-service.
 *
 * <p>Note que ele sobe SEM os vizinhos no ar: os RestClient sao preguicosos,
 * so abrem conexao quando chamados.</p>
 */
@SpringBootTest
class CambioServiceApplicationTest {

    @Test
    @DisplayName("o contexto do cambio-service sobe sem os vizinhos")
    void contextoSobe() {
    }
}
