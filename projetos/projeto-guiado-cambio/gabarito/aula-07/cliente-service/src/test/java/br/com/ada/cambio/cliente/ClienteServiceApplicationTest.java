package br.com.ada.cambio.cliente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Topo da piramide: sobe o contexto inteiro com H2 em memoria. */
@SpringBootTest(properties = {
        // Teste nao fala com o mundo: nada de registrar no Eureka nem baixar registro.
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class ClienteServiceApplicationTest {

    @Test
    @DisplayName("o contexto do cliente-service sobe")
    void contextoSobe() {
        // Se qualquer bean estiver mal configurado, o teste falha aqui.
    }
}
