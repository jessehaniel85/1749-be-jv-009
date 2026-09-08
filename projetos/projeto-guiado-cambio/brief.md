# Projeto Guiado em Aula — API de Câmbio (ordem de compra de moeda estrangeira)

**O que é:** o projeto-referência do módulo BE-JV-009, **construído ao vivo** (live/mob coding) ao longo das Aulas 1–8. Não é avaliado — é a **demonstração canônica** de como uma API nasce ágil e monolítica, ganha testes, cresce, é quebrada em serviços que conversam entre si e, por fim, é refatorada com Clean Code, SOLID e Design Patterns. Apresentado na Aula 1.

> Este arquivo é a **fonte da verdade** do domínio, dos nomes e do incremento de cada aula. Planos de aula, materiais do aluno, gabarito e requisitos devem obedecer ao que está aqui.

## Problema (do planejamento oficial do módulo)

Uma instituição financeira quer oferecer a compra de **moeda estrangeira (USD e EUR)** pelo app, com **retirada em agência**. O cliente se cadastra, consulta a cotação e registra uma **ordem de compra**. O sistema calcula o valor em reais pela cotação do momento e devolve o comprovante da ordem.

### Domínios (linguagem ubíqua)

| Termo | Significado |
|---|---|
| **Cliente** | Pessoa cadastrada para operar: `nome`, `cpf` (11 dígitos, único), `dataNascimento`, `estadoCivil`, `sexo`. Recebe um `id` ao ser criado. |
| **Moeda** | `USD` ou `EUR` (enum). Qualquer outra sigla → `MoedaNaoSuportadaException` → HTTP `422`. |
| **Cotação** | Valor de 1 unidade da moeda em BRL, com `dataHora` da cotação. Vem de um **provedor externo** (`https://economia.awesomeapi.com.br/USD/`), **que não é acessível da rede corporativa** → ver "Provedor de cotação" abaixo. |
| **Ordem de compra** | Pedido de compra: `cpfCliente`, `moeda`, `valorMoedaEstrangeira`, `numeroAgenciaRetirada` (4 dígitos). O sistema calcula `valorCotacao` e `valorTotalOperacao = valorMoedaEstrangeira × valorCotacao` (arredondado a 2 casas, `HALF_EVEN`), registra `dataSolicitacao` e devolve `201`. |

### Endpoints de referência

```
POST /clientes                      → 201 { id, nome, cpf, dataNascimento, estadoCivil, sexo }
GET  /clientes/{cpf}                → 200 | 404
GET  /cotacoes/{moeda}              → 200 { moeda, valorCotacao, dataHora } | 422 moeda não suportada
POST /ordens                        → 201 (body abaixo) | 404 cliente | 422 moeda/agência
GET  /ordens/{id}                   → 200 | 404
```

Resposta `201` de `POST /ordens` (contrato do módulo, campos em snake_case):

```json
{
  "id_compra": 1,
  "id_cliente": 1,
  "cpf_cliente": "43488428095",
  "dataSolicitacao": "2026-09-14T16:11:23.866",
  "tipo_moeda": "EUR",
  "valor_moeda_estrangeira": 100.0,
  "valor_cotacao": 6.5857,
  "valor_total_operacao": 658.57,
  "numero_agencia_retirada": "7057"
}
```

### Provedor de cotação (decisão de ambiente)

A rede corporativa **bloqueia sites externos**. Por isso o provedor de cotação é **um serviço nosso** (`cotacao-service` a partir da Aula 5; antes disso, um componente do monólito) que:

1. tenta o provedor externo (`awesomeapi`) **se** a propriedade `cotacao.provedor=externo` estiver ativa (só fora da rede corporativa);
2. caso contrário (padrão, `cotacao.provedor=local`) responde a partir de uma **tabela local** (`cotacoes` no H2, semeada em `data.sql` com USD ≈ 5,43 e EUR ≈ 6,58, atualizável por `PUT /cotacoes/{moeda}`).

Isso não é gambiarra: é o padrão **Adapter/Anti-Corruption Layer** que a Aula 4 (SOA × REST) e a Aula 8 (Design Patterns) exploram. O contrato interno (`CotacaoProvider`) é o mesmo nos dois casos.

## Como cresce, aula a aula

| Aula | Estado | Incremento (o que muda no código) |
|---|---|---|
| 1 | `aula-01` | **Monólito** `cambio-api` (Spring Boot 3, Java 21, H2). Só o domínio **Cliente**: `POST /clientes`, `GET /clientes/{cpf}`, validação (`jakarta.validation`), `@ControllerAdvice` para `404/400`. Backlog em Kanban (`KANBAN.md`) com as histórias do módulo; esta aula entrega as 2 primeiras. |
| 2 | `aula-02` | **Testes**: unitários de `ClienteService` (JUnit 5 + Mockito), teste de controller (`@WebMvcTest` + MockMvc), 1 teste de integração (`@SpringBootTest` + H2). `mvn test` verde. `README` explica **em que nível da pirâmide** cada teste está. |
| 3 | `aula-03` | **Monólito cresce**: domínios **Cotação** (tabela local + `GET /cotacoes/{moeda}`) e **Ordem** (`POST /ordens`, `GET /ordens/{id}`), no mesmo projeto, pacotes por domínio (`cliente`, `cotacao`, `ordem`). `OrdemService` chama `ClienteService` e `CotacaoService` **por injeção direta**. Testes acompanham. |
| 4 | `aula-04` | **SOA × REST**: `CotacaoProvider` vira **contrato de serviço** (interface) com duas implementações — `CotacaoLocalProvider` (H2) e `CotacaoExternaProvider` (`RestClient` → awesomeapi, com **Adapter** do JSON externo para o modelo interno). Seleção por propriedade. `PUT /cotacoes/{moeda}` para simular variação. Documentação do contrato (OpenAPI via springdoc, se resolver no Nexus; senão `docs/contrato-cotacao.md`). |
| 5 | `aula-05` | **Quebra do monólito** em 3 microsserviços Maven: `cliente-service` (8081), `cotacao-service` (8082), `cambio-service` (8083, dono de Ordem). **Bases H2 segregadas**. `cambio-service` chama os outros dois por `RestClient` com **URL fixa** em `application.yml`. Cada serviço tem seus testes. |
| 6 | `aula-06` | **Service discovery + client declarativo**: módulo `discovery-server` (Eureka, 8761); os 3 serviços registram-se (`spring-cloud-starter-netflix-eureka-client`); `cambio-service` troca `RestClient` por **OpenFeign** (`@FeignClient(name = "cliente-service")`, `@FeignClient(name = "cotacao-service")`). **Plano C** (se Spring Cloud não resolver no Nexus): manter `RestClient` com interfaces `@HttpExchange` e um "registro estático" em `application.yml` — mesmo desenho, sem Eureka. |
| 7 | `aula-07` | **Clean Code + SOLID** (refatoração sem mudar comportamento — testes garantem): nomes, funções curtas, constantes (sem números mágicos: 4 dígitos da agência, 11 do CPF, escala 2), **injeção por construtor** (fim do `@Autowired` em campo), **SRP** (`OrdemService` separa validação/cálculo/persistência), **OCP/LSP** (`CotacaoProvider` e suas implementações), **ISP** (interfaces pequenas: `ConsultaCliente` × `CadastroCliente`), **DIP** (serviço depende da abstração, não do Feign). |
| 8 | `aula-08` | **Design Patterns** aplicados: **Strategy** (`CalculoOperacaoStrategy` por moeda — USD e EUR com regra de arredondamento/spread distinta, registrado em `Map<Moeda, Strategy>`), **Facade** (`CambioFacade` esconde a orquestração cliente → cotação → ordem do controller), **Singleton** (discussão: o escopo de bean do Spring já é singleton; um `CatalogoMoedas` como singleton explícito × bean), **Adapter** (já existente desde a Aula 4, agora nomeado), **Builder** (opcional, no DTO de resposta da ordem). ADR curto por padrão em `docs/adr/`. **Atenção:** a estratégia EUR aplica spread de 0,5% → o total de 100 EUR a 6,5857 passa a ser 661,86 (o exemplo `658.57` acima vale até a Aula 7). É funcionalidade nova (US-10), não refatoração — contraste didático com a Aula 7. |

> **Regra de estados:** cada pasta `gabarito/aula-0X/` é o estado **completo e compilável** ao fim daquela aula (perfil padrão, sem Docker). O esqueleto em `inicio/` é o estado de partida da Aula 1 com `// TODO`.

## Stack e ambiente

- **Java 21** · **Maven** · **Spring Boot 3.5.x** · H2 · `spring-boot-starter-web`, `-data-jpa`, `-validation`, `-test`.
- **Spring Cloud 2025.x** (Aula 6): `spring-cloud-starter-netflix-eureka-server`, `-eureka-client`, `spring-cloud-starter-openfeign`.
- **Nenhuma infra externa** (sem Docker, sem broker, sem banco externo). Tudo roda com `mvn spring-boot:run` em portas locais.
- **Nexus corporativo** resolve os artefatos (`ambiente/settings.xml`). Risco único deste módulo: Spring Cloud (ver `ambiente/checklist-semana-0.md`, seção 3).
- **Pacote raiz:** `br.com.ada.cambio` (cliente-agnóstico). Sub-pacotes por domínio: `cliente`, `cotacao`, `ordem`; por camada dentro do domínio: `api` (controller/DTO), `dominio` (entidade/regra), `infra` (repositório/adapters).

## Papel didático

- É a **fonte da verdade** dos padrões: o grupo pode (e deve) se inspirar, **sem copiar tema**.
- O **gabarito completo é liberado no início** do módulo — vira material de estudo e dissecação ("por que assim? que alternativa havia?"), não build & reveal.
- Cada estado fica pronto para **colar** se o live coding travar (rede do Teams).
- O **Kanban do projeto** (`KANBAN.md` + board no Teams/Planner) é mantido ao vivo do início ao fim: mover card é parte da aula, não cerimônia decorativa.

## Estrutura do repositório

```
projeto-guiado-cambio/
├── brief.md                       # este arquivo (spec do domínio + incrementos)
├── requisitos/                    # personas, transcrições com a PO e user stories (eng. de requisitos)
├── inicio/                        # esqueleto da Aula 1 (monólito com TODOs)
└── gabarito/
    ├── README.md                  # como rodar cada estado
    ├── aula-01/  cambio-api/      # monólito: Cliente
    ├── aula-02/  cambio-api/      # + testes (pirâmide)
    ├── aula-03/  cambio-api/      # + Cotação + Ordem (monólito com 3 domínios)
    ├── aula-04/  cambio-api/      # + CotacaoProvider (contrato, local × externo, Adapter)
    ├── aula-05/  {cliente,cotacao,cambio}-service/   # 3 microsserviços, URL fixa
    ├── aula-06/  + discovery-server/                 # Eureka + OpenFeign
    ├── aula-07/  ...                                 # Clean Code + SOLID
    └── aula-08/  ...                                 # Strategy, Facade, Singleton, Adapter + ADRs
```
