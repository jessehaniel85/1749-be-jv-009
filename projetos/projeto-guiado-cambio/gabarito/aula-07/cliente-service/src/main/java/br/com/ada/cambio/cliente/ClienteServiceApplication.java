package br.com.ada.cambio.cliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microsservico dono do dominio Cliente (porta 8081).
 *
 * <p>Depois da quebra do monolito (Aula 5) este servico e o unico que enxerga
 * a tabela {@code clientes}. Ninguem mais le ou escreve nela: quem precisar de
 * um cliente pergunta pela API.</p>
 */
@SpringBootApplication
public class ClienteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClienteServiceApplication.class, args);
    }
}
