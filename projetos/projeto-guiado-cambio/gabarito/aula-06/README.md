# aula-06 — Service discovery (Eureka) + cliente declarativo (OpenFeign)

Estado **completo e compilável** da API de Câmbio ao fim da Aula 6.

```
aula-06/
├── pom.xml                 # POM pai: Boot 3.5.9 + BOM Spring Cloud 2025.0.3
├── discovery-server/       # 8761 · Eureka Server (sem Docker, é um app Spring)
├── cliente-service/        # 8081 · registra-se no Eureka
├── cotacao-service/        # 8082 · registra-se no Eureka
├── cambio-service/         # 8083 · registra-se e CONSOME os outros por Feign
├── docs/adr/               # ADR-001..004 (o histórico completo de decisões)
├── KANBAN.md               # US-01..08 DONE
└── ESTADO.md               # subir na ordem, painel do Eureka, Plano C
```

## As duas ideias da aula

**1. Descoberta.** Ninguém escreve `http://localhost:8081` em lugar nenhum. Cada serviço
diz seu `spring.application.name` ao subir; quem precisa dele pergunta ao catálogo.

**2. Cliente declarativo.** A chamada remota vira uma **interface**:

```java
@FeignClient(name = "cliente-service", url = "${servicos.cliente.url:}")
public interface ClienteClient {
    @GetMapping("/clientes/{cpf}")
    ClienteResumo buscarPorCpf(@PathVariable("cpf") String cpf);
}
```

Compare com a versão da Aula 5 (`aula-05/.../infra/ClienteClient.java`): sumiram `RestClient`,
`baseUrl`, `uri(...)`, `retrieve()` e o `try/catch`. Sobrou o **contrato**.

## Versões

| Peça | Versão | Por quê |
|---|---|---|
| Spring Boot | `3.5.9` | mesma linha das aulas anteriores |
| Spring Cloud | `2025.0.3` | trem compatível com Boot **3.5.x** — o BOM fica no `dependencyManagement` do POM pai |

Trem errado é a fonte nº 1 de `NoClassDefFoundError` em projeto Spring Cloud: o BOM **não**
traz dependência nenhuma, ele só diz *qual versão usar se você pedir o artefato*. Por isso os
módulos declaram os starters **sem `<version>`**.

## Rodar

```bash
mvn clean test                                   # os 4 módulos
mvn -pl discovery-server spring-boot:run         # PRIMEIRO — painel em :8761
mvn -pl cliente-service  spring-boot:run
mvn -pl cotacao-service  spring-boot:run
mvn -pl cambio-service   spring-boot:run
```

Sem Eureka (Plano C), três terminais:

```bash
mvn -pl cliente-service spring-boot:run -Dspring-boot.run.profiles=plano-c
mvn -pl cotacao-service spring-boot:run -Dspring-boot.run.profiles=plano-c
mvn -pl cambio-service  spring-boot:run -Dspring-boot.run.profiles=plano-c
```

Na rede corporativa, acrescente `-s ../../../../ambiente/settings.xml`.

Demonstrações de aula, cURLs e o passo a passo do Plano C: **`ESTADO.md`**.
Por que Eureka e por que Feign, com as alternativas descartadas: **`docs/adr/ADR-004-service-discovery-e-feign.md`**.
