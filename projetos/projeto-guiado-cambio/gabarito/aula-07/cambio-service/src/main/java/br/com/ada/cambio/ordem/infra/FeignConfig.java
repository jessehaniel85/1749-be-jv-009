package br.com.ada.cambio.ordem.infra;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Liga a varredura de {@code @FeignClient}.
 *
 * <p>Por que aqui e nao na classe {@code @SpringBootApplication}? Porque
 * {@code @WebMvcTest} carrega a classe principal para achar a configuracao, mas
 * NAO carrega a auto-configuracao do Feign. Deixando {@code @EnableFeignClients}
 * numa {@code @Configuration} separada - que a fatia web nao varre - os testes
 * de controller continuam leves e verdes.</p>
 */
@Configuration
@EnableFeignClients(basePackages = "br.com.ada.cambio.ordem.infra")
public class FeignConfig {
}
