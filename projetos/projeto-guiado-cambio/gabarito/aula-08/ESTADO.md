# Estado ao fim da Aula 8 — padrões nomeados, projeto concluído

Este é o **último** estado do projeto guiado. O que entrou aqui: Strategy, Facade, Singleton,
Builder — e o Adapter, que já existia desde a Aula 4, ganhou nome no catálogo.

Catálogo completo (qual padrão, em qual classe, resolvendo qual problema, e os que **não**
usamos e por quê): **`docs/patterns-no-projeto.md`**.

## ⚠️ A única mudança de comportamento desde a Aula 5

O **euro passou a ter spread de 0,5%**. Uma ordem de 100,00 EUR a 6,5857:

| | Aulas 5–7 | Aula 8 |
|---|---|---|
| `valor_total_operacao` | `658.57` | **`661.86`** |

Conta: `100.00 × 6.5857 = 658.5700`, `× 1.005 = 661.862850`, `HALF_EVEN(2) = 661.86`.
O **dólar continua sem spread** (`100.00 × 5.4321 = 543.21`).

> **Isso é o oposto do que a Aula 7 fez, e é de propósito.** A Aula 7 foi *refatoração*:
> mesmo comportamento, código melhor. A Aula 8 é *funcionalidade nova*: a mesa de câmbio
> pediu uma regra comercial. Saber distinguir as duas é conteúdo da aula — e é o que separa
> um daily honesto de um daily onde "refatorei" quer dizer "mudei e não avisei".
>
> Consequência prática: o payload de exemplo do `brief.md` (que mostra `658.57`) descreve o
> comportamento das **Aulas 3 a 7**. A partir da Aula 8, EUR sai com spread.

## O que mudou, em uma tabela

| | Aula 7 | Aula 8 |
|---|---|---|
| Cálculo | uma fórmula para todas as moedas | `CalculoUsdStrategy` / `CalculoEurStrategy` num `Map<Moeda, Strategy>` |
| Dependências do controller | `OrdemService` | **só** `CambioFacade` |
| `OrdemService` | orquestrava + persistia | só persiste |
| Catálogo de moedas | `Moeda.deSigla` chamado direto | `CatalogoDeMoedas` injetado (3 implementações, 1 usada) |
| `OrdemResponse` | construtor canônico | **+** `OrdemResponse.construtor()` (Builder) |
| Adapters | existiam desde a Aula 7 | os mesmos, agora **nomeados** como pattern |

## Como rodar (igual às Aulas 6 e 7)

```bash
cd aula-08
mvn -pl discovery-server spring-boot:run   # 8761 — primeiro
mvn -pl cliente-service  spring-boot:run   # 8081
mvn -pl cotacao-service  spring-boot:run   # 8082
mvn -pl cambio-service   spring-boot:run   # 8083
```

Plano C (sem Eureka): `-Dspring-boot.run.profiles=plano-c` nos três serviços.
Na rede corporativa, acrescente `-s ../../../../ambiente/settings.xml`.

## A demonstração da aula: as duas moedas lado a lado

```bash
curl -s -X POST http://localhost:8081/clientes -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Souza","cpf":"43488428095","dataNascimento":"1990-05-10",
       "estadoCivil":"SOLTEIRO","sexo":"FEMININO"}'

# EUR — com spread de 0,5%
curl -s -X POST http://localhost:8083/ordens -H 'Content-Type: application/json' \
  -d '{"cpf_cliente":"43488428095","tipo_moeda":"EUR",
       "valor_moeda_estrangeira":100.00,"numero_agencia_retirada":"7057"}'
# → "valor_cotacao": 6.5857,  "valor_total_operacao": 661.86

# USD — sem spread
curl -s -X POST http://localhost:8083/ordens -H 'Content-Type: application/json' \
  -d '{"cpf_cliente":"43488428095","tipo_moeda":"USD",
       "valor_moeda_estrangeira":100.00,"numero_agencia_retirada":"7057"}'
# → "valor_cotacao": 5.4321,  "valor_total_operacao": 543.21
```

Duas moedas, dois resultados, **zero `if` no código**. Mostre `CalculadoraDeOperacao`
projetada: são doze linhas, e nenhuma pergunta "qual moeda é esta?".

### O exercício de 3 minutos que fecha o módulo

Peça à turma para adicionar **GBP** ao vivo:

1. `Moeda`: acrescentar a constante `GBP`;
2. criar `CalculoGbpStrategy implements CalculoOperacaoStrategy` com `@Component`;
3. `INSERT` da cotação no `data.sql` do `cotacao-service` (ou `PUT /cotacoes/GBP`).

**Nenhum arquivo existente é editado** além do enum e do seed. Nem a config das estratégias,
nem a calculadora, nem a facade, nem o controller, nem um teste que já passava.

E se alguém "esquecer" o passo 2: a aplicação **não sobe** — a `EstrategiasDeCalculoConfig`
recusa uma moeda do catálogo sem estratégia. Vale provocar o erro de propósito: falha na
subida é infinitamente melhor que falha na primeira ordem em produção.

## Singleton: os três, lado a lado

`CatalogoMoedas` (clássico, `getInstance()`), `CatalogoMoedasEnum` (enum) e
`CatalogoMoedasBean` (`@Component`). Os três têm o mesmo comportamento —
`CatalogoDeMoedasTest` prova isso num laço. **O que roda é o bean.**

A pergunta que vale cinco minutos: *se o escopo padrão do Spring já é singleton, o padrão do
GoF morreu?* Resposta em `docs/patterns-no-projeto.md`: não morreu — continua valendo onde
não há container. Mas **dentro** de uma aplicação Spring, escrever `getInstance()` à mão troca
uma dependência declarada por uma escondida, e o teste perde a capacidade de substituí-la.

## Testes

```bash
cd aula-08 && mvn clean test
```

Novos nesta aula, no `cambio-service`:

| Classe | O que garante |
|---|---|
| `CalculoUsdStrategyTest` | dólar sem spread, HALF_EVEN |
| `CalculoEurStrategyTest` | euro com 0,5%, arredondamento uma única vez, euro > dólar |
| `CalculadoraDeOperacaoTest` | despacho correto por moeda; moeda sem estratégia → 422 |
| `CambioFacadeTest` | a coreografia — inclusive **validar antes de gastar rede** |
| `CatalogoDeMoedasTest` | identidade das três formas de singleton e imutabilidade do catálogo |
| `OrdemResponseTest` | Builder: a ordem das chamadas não importa |

`OrdemServiceTest` **encolheu** para três testes de persistência — a orquestração migrou para
`CambioFacadeTest`. Teste que diminui depois de uma extração é sinal de que a extração foi na
costura certa.
