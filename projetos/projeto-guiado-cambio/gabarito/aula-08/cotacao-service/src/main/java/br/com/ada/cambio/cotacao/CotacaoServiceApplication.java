package br.com.ada.cambio.cotacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microsservico dono do dominio Cotacao (porta 8082).
 *
 * <p>E a nossa camada anticorrupcao para o mundo externo: quem consome cotacao
 * fala com este servico e nunca com a awesomeapi.</p>
 */
@SpringBootApplication
public class CotacaoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CotacaoServiceApplication.class, args);
    }
}
