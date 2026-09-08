# aula-05 — Quebra do monólito em três microsserviços

Estado **completo e compilável** da API de Câmbio ao fim da Aula 5.

```
aula-05/
├── pom.xml                 # POM pai agregador (Boot 3.5.9, Java 21)
├── cliente-service/        # 8081 · dono de Cliente   · H2 clientedb
├── cotacao-service/        # 8082 · dono de Cotação   · H2 cotacaodb
├── cambio-service/         # 8083 · dono de Ordem     · H2 cambiodb
├── docs/                   # ADR-001..003 (herdados + o desta aula) e contrato-cotacao.md
├── KANBAN.md               # US-01..07 DONE
└── ESTADO.md               # como subir, cURLs, demonstração do 503
```

## Mapa de portas e responsabilidades

| Serviço | Porta | Endpoints | Base | Fala com |
|---|---|---|---|---|
| `cliente-service` | 8081 | `POST /clientes`, `GET /clientes/{cpf}` | `clientedb` | ninguém |
| `cotacao-service` | 8082 | `GET /cotacoes/{moeda}`, `PUT /cotacoes/{moeda}` | `cotacaodb` | awesomeapi (só se `cotacao.provedor=externo`) |
| `cambio-service` | 8083 | `POST /ordens`, `GET /ordens/{id}` | `cambiodb` | 8081 e 8082 |

## Arquitetura de pacotes (igual nos três)

```
br.com.ada.cambio.<dominio>
├── api      → controller, DTOs (record), @RestControllerAdvice
├── dominio  → entidade JPA, service, enums, exceções de negócio
└── infra    → repositório Spring Data, clients HTTP, adapters
```

A regra que segura tudo: **`api` e `infra` conhecem `dominio`; `dominio` não conhece `api`**.
Na Aula 5 o `dominio` ainda enxerga `infra` (`OrdemService` importa `ClienteClient`) — é
exatamente esse fio solto que a **Aula 7** corta com DIP.

## Rodar

```bash
mvn clean test                                 # os três módulos
mvn -pl cliente-service spring-boot:run        # um serviço por terminal
```

Na rede corporativa, acrescente `-s ../../../../ambiente/settings.xml`.

Passo a passo com cURL, a demonstração do 503 e o que este estado ainda não faz: **`ESTADO.md`**.

## Provedor de cotação

Padrão (`cotacao.provedor=local`): lê a tabela `cotacoes`, semeada por `data.sql` com
USD 5.4321 e EUR 6.5857 e alterável por `PUT /cotacoes/{moeda}`.

Fora da rede corporativa, `cotacao.provedor=externo` liga o `CotacaoExternaProvider`
(awesomeapi, timeout 3s, `AwesomeApiAdapter` traduzindo o JSON externo):

```bash
mvn -pl cotacao-service spring-boot:run -Dspring-boot.run.arguments=--cotacao.provedor=externo
```

Quem consome não percebe a diferença: o contrato `CotacaoProvider` é o mesmo.
