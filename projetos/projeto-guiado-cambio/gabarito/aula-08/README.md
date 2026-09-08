# aula-08 — Design Patterns (Strategy, Facade, Singleton, Adapter, Builder)

Estado **final** da API de Câmbio. Mesmos 4 módulos das Aulas 6 e 7.

```
aula-08/
├── pom.xml                              # Boot 3.5.9 + Spring Cloud 2025.0.3
├── discovery-server/                    # 8761 · Eureka
├── cliente-service/                     # 8081
├── cotacao-service/                     # 8082 · AwesomeApiAdapter (Adapter desde a Aula 4)
├── cambio-service/                      # 8083 · Strategy, Facade, Singleton, Builder
├── docs/
│   ├── antes-depois-solid.md            # (Aula 7)
│   ├── patterns-no-projeto.md           # tabela pattern → classe → por quê + o que NÃO usamos
│   ├── contrato-cotacao.md              # contrato do cotacao-service (Aula 4)
│   └── adr/                             # ADR-001 .. ADR-007 (histórico completo)
├── KANBAN.md                            # US-01..10 DONE — backlog concluído
└── ESTADO.md                            # o spread do euro, a demo das duas moedas, o exercício do GBP
```

## Os padrões, em uma tabela

| Padrão | Onde | Problema que resolveu |
|---|---|---|
| **Strategy** | `CalculoOperacaoStrategy` + `CalculoUsdStrategy` / `CalculoEurStrategy` + `EstrategiasDeCalculoConfig` | regra comercial por moeda sem `switch` que cresce |
| **Facade** | `CambioFacade` | o controller conhecia 5 colaboradores e a ordem entre eles |
| **Adapter** | `AwesomeApiAdapter`, `ClienteClientAdapter`, `CotacaoClientAdapter` | traduzir dado **e erro** do outro para o nosso |
| **Singleton** | `CatalogoMoedasBean` (usado) · `CatalogoMoedas`, `CatalogoMoedasEnum` (didáticos) | fonte única do catálogo — e por que o bean ganha do `getInstance()` |
| **Builder** | `OrdemResponse.Construtor` | 9 componentes, 3 `BigDecimal` adjacentes |

Detalhes, contra-exemplos e a lista dos padrões que **não** usamos (com o motivo):
**`docs/patterns-no-projeto.md`**.

## ⚠️ Mudança de comportamento nesta aula

O euro passou a ter **spread de 0,5%**: 100,00 EUR a 6,5857 agora dá **661,86** (era 658,57).
O dólar segue sem spread. É funcionalidade nova (US-10), não refatoração — a distinção está
explicada em `ESTADO.md` e no `ADR-006`.

## Rodar

```bash
mvn clean test
mvn -pl discovery-server spring-boot:run   # e depois os três serviços
```

Na rede corporativa, acrescente `-s ../../../../ambiente/settings.xml`.
A demonstração das duas moedas e o exercício "adicione GBP em 3 minutos": **`ESTADO.md`**.
