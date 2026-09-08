# Estado após a Aula 3 — o monólito cresce para três domínios

**Foco:** o produto ficou útil. Com `cotacao` e `ordem`, a API faz o que o negócio pediu: cliente consulta a cotação e registra uma ordem de compra com retirada em agência.

## O que funciona

Tudo das Aulas 1 e 2, mais:

| Método | Rota | Resposta |
|---|---|---|
| `GET` | `/cotacoes/{moeda}` | `200` `{moeda, valorCotacao, dataHora}` · `422` sigla fora de USD/EUR |
| `POST` | `/ordens` | `201` comprovante snake_case · `400` corpo inválido · `404` cliente · `422` moeda ou agência |
| `GET` | `/ordens/{id}` | `200` · `404` |

- **Cotação local:** tabela `cotacoes` semeada por `data.sql` com **USD 5,4321** e **EUR 6,5857**.
- **Cálculo:** `valorTotalOperacao = valorMoedaEstrangeira × valorCotacao`, `setScale(2, HALF_EVEN)`.
- **Orquestração:** `OrdemService` injeta `ClienteService` e `CotacaoService` **direto** — é monólito, e isso é uma decisão registrada em [`docs/adr/ADR-001-monolito-por-enquanto.md`](docs/adr/ADR-001-monolito-por-enquanto.md).
- **27 testes** verdes (`mvn test`), incluindo `OrdemServiceTest`, `OrdemControllerTest` e `CotacaoControllerTest`.

## Detalhe que vale a discussão: 400 × 422

`GET /cotacoes/{moeda}` recebe **`String`**, não `Moeda`. Se o path variable fosse tipado como enum, o Spring recusaria `JPY` com **400** antes de o domínio ver a requisição — e o contrato do módulo pede **422**. O mesmo vale para `numeroAgenciaRetirada`, que **não** tem `@Pattern`: quem valida é o `OrdemService`, porque só ele sabe lançar `AgenciaInvalidaException`.

Regra: **400** = não entendi a requisição. **422** = entendi, e o negócio recusa.

## Como rodar

```bash
export JAVA_HOME=$HOME/.sdkman/candidates/java/21-open
mvn clean test          # 27 testes
mvn spring-boot:run
```

## Exemplos cURL

```bash
# 1) cadastrar o cliente
curl -i -X POST localhost:8080/clientes -H 'Content-Type: application/json' -d '{
  "nome":"Marina Alcântara","cpf":"43488428095","dataNascimento":"1991-04-17",
  "estadoCivil":"SOLTEIRO","sexo":"FEMININO"}'

# 2) consultar a cotação
curl -s localhost:8080/cotacoes/EUR
# {"moeda":"EUR","valorCotacao":6.5857,"dataHora":"..."}

curl -i localhost:8080/cotacoes/JPY      # → 422

# 3) registrar a ordem
curl -i -X POST localhost:8080/ordens -H 'Content-Type: application/json' -d '{
  "cpf":"43488428095","moeda":"EUR","valorMoedaEstrangeira":100.0,
  "numeroAgenciaRetirada":"7057"}'
# → 201
# {"id_compra":1,"id_cliente":1,"cpf_cliente":"43488428095","dataSolicitacao":"...",
#  "tipo_moeda":"EUR","valor_moeda_estrangeira":100.0,"valor_cotacao":6.5857,
#  "valor_total_operacao":658.57,"numero_agencia_retirada":"7057"}

curl -i localhost:8080/ordens/1          # → 200
curl -i localhost:8080/ordens/99         # → 404

# agência com 3 dígitos → 422
curl -i -X POST localhost:8080/ordens -H 'Content-Type: application/json' -d '{
  "cpf":"43488428095","moeda":"EUR","valorMoedaEstrangeira":100.0,
  "numeroAgenciaRetirada":"705"}'
```

## Perguntas para a turma

- Por que a ordem guarda `valorCotacao` e `cpfCliente` copiados, em vez de só uma FK para cliente e cotação?
- `HALF_EVEN` em vez de `HALF_UP`: que diferença isso faz em um milhão de operações?
- `OrdemService` poderia injetar `ClienteRepository` direto e economizar uma camada. O que se perde?

## Próxima aula

**US-06** — a origem da cotação vira um **contrato** (`CotacaoProvider`), com implementação local (H2) e externa (awesomeapi via `RestClient` + Adapter), escolhida por propriedade. É o ensaio da separação em serviços da Aula 5.
