package br.com.ada.cambio.ordem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microsservico dono do dominio Ordem de compra (porta 8083).
 *
 * <p>E o unico que orquestra: consulta o cliente-service, consulta o
 * cotacao-service, calcula o total e grava a ordem na SUA base.</p>
 */
@SpringBootApplication
public class CambioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CambioServiceApplication.class, args);
    }
}
