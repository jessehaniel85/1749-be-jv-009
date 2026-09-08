# aula-07 — Clean Code + SOLID (refatoração sem mudar comportamento)

Estado **completo e compilável** da API de Câmbio ao fim da Aula 7.
Mesmos 4 módulos da Aula 6, mesmas respostas HTTP, código reorganizado.

```
aula-07/
├── pom.xml                       # Boot 3.5.9 + Spring Cloud 2025.0.3
├── discovery-server/             # 8761 · Eureka (inalterado)
├── cliente-service/              # 8081 · ConsultaCliente + CadastroCliente (ISP)
├── cotacao-service/              # 8082 · CotacaoProvider com contrato explícito (OCP/LSP)
├── cambio-service/               # 8083 · SRP + DIP: portas no domínio, Adapters na infra
├── docs/
│   ├── antes-depois-solid.md     # 5 princípios, trecho antes/depois de cada um
│   ├── contrato-cotacao.md       # contrato do cotacao-service (Aula 4)
│   └── adr/                      # ADR-001 .. ADR-005
├── KANBAN.md                     # US-01..09 DONE
└── ESTADO.md                     # o que mudou e a prova de que o comportamento não mudou
```

## O desenho do `cambio-service` depois da inversão

```
        api                    dominio                     infra
  ┌─────────────┐        ┌──────────────────┐       ┌─────────────────────┐
  │OrdemControl.│───────▶│ OrdemService     │       │ ClienteFeignClient  │
  │TratadorDe.. │        │ ValidadorDeOrdem │       │ CotacaoFeignClient  │
  └─────────────┘        │ CalculadoraDe... │       │ OrdemRepository     │
                         │                  │       │                     │
                         │ «interface»      │◀──────│ ClienteClientAdapter│
                         │ ConsultaCliente  │       │ CotacaoClientAdapter│
                         │ ConsultaCotacao  │       └─────────────────────┘
                         └──────────────────┘        (único lugar com feign)
```

Todas as setas apontam para `dominio`. Ele não importa `api`, não importa `feign` e só
conhece de `infra` o seu próprio repositório.

## As mudanças, por princípio

| Princípio | O que mudou |
|---|---|
| **Construtor** | já era o padrão; contra-exemplo com `@Autowired` em campo documentado |
| **SRP** | `OrdemService` → `ValidadorDeOrdem` + `CalculadoraDeOperacao` + `OrdemService` |
| **OCP** | `CotacaoProvider` reforçado — fonte nova é arquivo novo, sem `if` de tipo |
| **LSP** | pré/pós-condições escritas no Javadoc da interface |
| **ISP** | `ConsultaCliente` × `CadastroCliente`; no câmbio existe só a de leitura |
| **DIP** | portas no `dominio`, `*ClientAdapter` na `infra`, `feign` confinado |
| **Clean Code** | `RegrasDeCambio` / `RegrasDeCliente` no lugar dos números mágicos; funções curtas, um nível de abstração |

Cada um com trecho **antes/depois** real do repositório em **`docs/antes-depois-solid.md`**.

## Rodar

```bash
mvn clean test
mvn -pl discovery-server spring-boot:run   # e depois os três serviços
```

Na rede corporativa, acrescente `-s ../../../../ambiente/settings.xml`.
Detalhes, cURLs e a demonstração do `grep`: **`ESTADO.md`**.
