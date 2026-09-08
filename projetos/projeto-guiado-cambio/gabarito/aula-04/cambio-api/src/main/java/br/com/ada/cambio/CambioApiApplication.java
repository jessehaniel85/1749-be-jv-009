package br.com.ada.cambio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da API de câmbio.
 *
 * <p>Estado da Aula 1: monólito com um único domínio ({@code cliente}). Os domínios
 * {@code cotacao} e {@code ordem} entram na Aula 3, no mesmo projeto.</p>
 */
@SpringBootApplication
public class CambioApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CambioApiApplication.class, args);
    }
}
