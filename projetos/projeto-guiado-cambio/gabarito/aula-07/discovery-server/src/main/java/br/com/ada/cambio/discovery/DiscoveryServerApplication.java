package br.com.ada.cambio.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Catalogo telefonico do sistema (Eureka Server, porta 8761).
 *
 * <p>E so mais um app Spring Boot: {@code mvn spring-boot:run}. Nao precisa de
 * Docker nem de infra externa - por isso ele cabe nesta turma.</p>
 *
 * <p>Cada servico, ao subir, liga para ca e diz "sou o cliente-service, estou em
 * tal IP e porta". Quem precisa falar com o cliente-service pergunta aqui em vez
 * de ter a URL escrita no proprio arquivo de configuracao.</p>
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
