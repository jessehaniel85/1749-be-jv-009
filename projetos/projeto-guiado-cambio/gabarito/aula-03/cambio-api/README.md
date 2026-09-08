# cambio-api — Aula 3 (monólito com 3 domínios)

Estado **completo e compilável** ao fim da Aula 3 do projeto guiado **BE-JV-009**.

- **Stack:** Java 21 · Spring Boot 3.5.9 · Maven · H2 em memória. Sem Docker.
- **Rodar:** `mvn clean test` (27 testes) · `mvn spring-boot:run`

## Endpoints

| Método | Rota | Resposta |
|---|---|---|
| `POST` | `/clientes` | `201` + `Location` · `400` · `409` |
| `GET` | `/clientes/{cpf}` | `200` · `404` |
| `GET` | `/cotacoes/{moeda}` | `200` · `422` moeda não operada |
| `POST` | `/ordens` | `201` comprovante · `400` · `404` cliente · `422` moeda/agência |
| `GET` | `/ordens/{id}` | `200` · `404` |

## Estrutura

```
src/main/java/br/com/ada/cambio/
├── CambioApiApplication.java
├── cliente/   api · dominio · infra        Cliente, ClienteService, ClienteRepository
├── cotacao/   api · dominio · infra        Cotacao, Moeda, CotacaoService, CotacaoRepository
├── ordem/     api · dominio · infra        OrdemDeCompra, OrdemService, OrdemRepository
└── comum/api                               TratadorDeErros, RespostaDeErro, ErroDeCampo
src/main/resources/
├── application.yml                         H2 + defer-datasource-initialization
└── data.sql                                semeia USD 5.4321 e EUR 6.5857
docs/adr/ADR-001-monolito-por-enquanto.md
```

**Um pacote por domínio, três camadas dentro de cada um.** É o mesmo recorte que a Aula 5 vai usar para separar os serviços — a fronteira já está desenhada, só ainda não custa uma chamada de rede.

## Testes (27)

| Classe | Nível |
|---|---|
| `ClienteServiceTest`, `OrdemServiceTest` | unitário (Mockito) |
| `ClienteControllerTest`, `OrdemControllerTest`, `CotacaoControllerTest` | fatia web (`@WebMvcTest`) |
| `ClienteIntegracaoTest` | integração (`@SpringBootTest` + H2) |

Detalhes, cURL e perguntas de aula: **[ESTADO.md](ESTADO.md)** · Board: **[KANBAN.md](KANBAN.md)**
