# cambio-api — Aula 4 (contrato do provedor de cotação)

Estado **completo e compilável** ao fim da Aula 4 do projeto guiado **BE-JV-009**.

- **Stack:** Java 21 · Spring Boot 3.5.9 · Maven · H2 em memória. Sem Docker.
- **Rodar:** `mvn clean test` (37 testes) · `mvn spring-boot:run`

## A ideia da aula

A cotação passa a ser consumida por um **contrato**, não por uma classe concreta:

```
              cotacao.dominio                       cotacao.infra
   CotacaoService ──▶ CotacaoProvider ◀────┬── CotacaoLocalProvider    (H2, padrão)
                        (interface)        └── CotacaoExternaProvider  (awesomeapi)
                                                      └─▶ AwesomeApiAdapter
```

Trocar a origem do dado é trocar **uma linha de YAML** — nenhuma classe de domínio muda:

```yaml
cotacao:
  provedor: local     # local (padrão, H2) | externo (awesomeapi, exige internet)
  externo:
    url: https://economia.awesomeapi.com.br/last
    timeout-segundos: 3
```

## Endpoints

| Método | Rota | Resposta |
|---|---|---|
| `POST` | `/clientes` | `201` + `Location` · `400` · `409` |
| `GET` | `/clientes/{cpf}` | `200` · `404` |
| `GET` | `/cotacoes/{moeda}` | `200` · `422` moeda não operada · `503` provedor externo fora |
| `PUT` | `/cotacoes/{moeda}` | `200` · `400` valor inválido · `422` moeda não operada |
| `POST` | `/ordens` | `201` comprovante · `400` · `404` cliente · `422` moeda/agência |
| `GET` | `/ordens/{id}` | `200` · `404` |

## Documentação

| Arquivo | O que é |
|---|---|
| [`docs/contrato-cotacao.md`](docs/contrato-cotacao.md) | contrato do serviço de cotação: operações, erros, implementações, compatibilidade |
| [`docs/adr/ADR-001-monolito-por-enquanto.md`](docs/adr/ADR-001-monolito-por-enquanto.md) | por que ainda é um deployable só |
| [`docs/adr/ADR-002-contrato-provedor-cotacao.md`](docs/adr/ADR-002-contrato-provedor-cotacao.md) | por que a interface mora no domínio |

## Testes (37)

| Classe | Nível | O que garante |
|---|---|---|
| `ClienteServiceTest`, `OrdemServiceTest` | unitário | regra de negócio |
| `AwesomeApiAdapterTest` | unitário, **sem rede** | tradução do modelo externo, incluindo os casos ruins |
| `ClienteControllerTest`, `OrdemControllerTest`, `CotacaoControllerTest` | fatia web | contrato HTTP |
| `SelecaoDoProvedorTest`, `SelecaoDoProvedorExternoTest` | integração | a propriedade realmente troca a implementação |
| `ClienteIntegracaoTest` | integração | fluxo ponta a ponta com H2 |

Detalhes, cURL e perguntas de aula: **[ESTADO.md](ESTADO.md)** · Board: **[KANBAN.md](KANBAN.md)**
