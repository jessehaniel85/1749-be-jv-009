# Estado após a Aula 4 — a cotação vira contrato (SOA × REST)

**Foco:** separar **o que o serviço promete** do **quem entrega**. A cotação passa a ser consumida por uma interface; a origem do dado é escolha de configuração.

## O que funciona

Tudo das Aulas 1 a 3, mais:

- **`CotacaoProvider`** (`cotacao.dominio`) — contrato com uma operação: `Cotacao obter(Moeda moeda)`.
- **`CotacaoLocalProvider`** — implementação padrão (`cotacao.provedor=local`, ou ausente). Lê o H2. **Não precisa de internet.**
- **`CotacaoExternaProvider`** — implementação alternativa (`cotacao.provedor=externo`). `RestClient` contra `https://economia.awesomeapi.com.br/last/{moeda}-BRL`, **timeout de 3s**.
- **`AwesomeApiAdapter`** — traduz o JSON do provedor externo para o nosso modelo. É a única classe que conhece `bid` e `create_date`.
- **`PUT /cotacoes/{moeda}`** `{"valorCotacao": 5.9}` → **200**, para simular variação de mercado ao vivo com o provedor local.
- **503** quando o provedor externo não responde utilmente (`CotacaoIndisponivelException`).
- **37 testes** verdes, entre eles `AwesomeApiAdapterTest` (6 casos, **sem rede**) e os dois testes que provam a seleção do provedor.

## Documentação

- [`docs/contrato-cotacao.md`](docs/contrato-cotacao.md) — o contrato do serviço em prosa: conceito, operações, erros, as duas implementações e a política de compatibilidade.
- [`docs/adr/ADR-002-contrato-provedor-cotacao.md`](docs/adr/ADR-002-contrato-provedor-cotacao.md) — por que a interface mora no domínio e por que o padrão é o provedor local.
- [`docs/adr/ADR-001-monolito-por-enquanto.md`](docs/adr/ADR-001-monolito-por-enquanto.md) — da Aula 3, ainda válido.

> Optamos por **markdown** em vez de springdoc/OpenAPI: uma dependência a menos para resolver no repositório de artefatos, e um documento que produto e QA leem sem ferramenta. O contrato descreve os mesmos elementos que um OpenAPI descreveria.

## Como rodar

```bash
export JAVA_HOME=$HOME/.sdkman/candidates/java/21-open
mvn clean test                       # 37 testes
mvn spring-boot:run                  # provedor local (padrão), sem internet

# com o provedor externo (só fora de rede que bloqueia a awesomeapi)
mvn spring-boot:run -Dspring-boot.run.arguments=--cotacao.provedor=externo
```

## Exemplos cURL

```bash
# cotação vigente (provedor local)
curl -s localhost:8080/cotacoes/USD
# {"moeda":"USD","valorCotacao":5.4321,"dataHora":"..."}

# simular alta do dólar
curl -i -X PUT localhost:8080/cotacoes/USD -H 'Content-Type: application/json' \
  -d '{"valorCotacao": 5.9000}'
# → 200 {"moeda":"USD","valorCotacao":5.9000,...}

# a próxima ordem já sai com o novo total
curl -i -X POST localhost:8080/ordens -H 'Content-Type: application/json' -d '{
  "cpf":"43488428095","moeda":"USD","valorMoedaEstrangeira":100.0,
  "numeroAgenciaRetirada":"7057"}'
# → valor_total_operacao: 590.00

# valor inválido no PUT → 400
curl -i -X PUT localhost:8080/cotacoes/USD -H 'Content-Type: application/json' \
  -d '{"valorCotacao": -1}'
```

## Perguntas para a turma

- A interface `CotacaoProvider` está em `dominio`, e as implementações em `infra`. Por que não o contrário?
- O `AwesomeApiAdapter` recebe um `Map` já desserializado, não a resposta HTTP. O que isso torna possível no teste?
- Timeout de 3 segundos: o que acontece com a API inteira se ele não existisse e a awesomeapi ficasse lenta?
- `@ConditionalOnProperty` × `@Profile`: quando cada um?

## Próxima aula

**US-07** — a quebra do monólito. `cliente`, `cotacao` e `cambio` viram três projetos Maven com bases H2 segregadas, e a chamada de método vira chamada HTTP. `CotacaoProvider` é a costura por onde o corte passa.
