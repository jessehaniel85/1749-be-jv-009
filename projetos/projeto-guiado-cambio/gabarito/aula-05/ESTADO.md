# Estado ao fim da Aula 5 — o monólito virou três serviços

## O que mudou desde a Aula 4

| Antes (aula-04) | Agora (aula-05) |
|---|---|
| 1 projeto `cambio-api`, 3 pacotes de domínio | 3 projetos Maven, 1 por domínio |
| 1 base H2 com 3 tabelas | **3 bases H2**: `clientedb`, `cotacaodb`, `cambiodb` |
| `OrdemService` chamava `ClienteService` por injeção | `OrdemService` chama o **cliente-service por HTTP** |
| Erro de negócio: 400/404/422 | **+ 503**: agora existe rede, e rede cai |

O ganho é autonomia (cada serviço sobe, escala e é deployado sozinho).
O preço aparece na próxima seção: **três terminais e uma nova classe de falha**.

## Como subir (ordem importa)

Três terminais, nesta ordem — o `cambio-service` é o único que depende dos outros:

```bash
# terminal 1
cd aula-05 && mvn -pl cliente-service spring-boot:run     # http://localhost:8081

# terminal 2
cd aula-05 && mvn -pl cotacao-service spring-boot:run     # http://localhost:8082

# terminal 3
cd aula-05 && mvn -pl cambio-service spring-boot:run      # http://localhost:8083
```

> Na rede da Caixa, acrescente `-s ../../../../ambiente/settings.xml` a cada comando.

Consoles H2 (usuário `sa`, senha vazia): `http://localhost:808X/h2-console`.

## Ponta a ponta com cURL

```bash
# 1) cadastrar o cliente (8081)
curl -i -X POST http://localhost:8081/clientes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Souza","cpf":"43488428095","dataNascimento":"1990-05-10",
       "estadoCivil":"SOLTEIRO","sexo":"FEMININO"}'
# → 201 { "id":1, "nome":"Ana Souza", ... }

# 2) conferir a cotação semeada pelo data.sql (8082)
curl -s http://localhost:8082/cotacoes/EUR
# → { "moeda":"EUR", "valorCotacao":6.5857, "dataHora":"..." }

# 3) registrar a ordem (8083) — ele consulta 8081 e 8082 por baixo dos panos
curl -i -X POST http://localhost:8083/ordens \
  -H 'Content-Type: application/json' \
  -d '{"cpf_cliente":"43488428095","tipo_moeda":"EUR",
       "valor_moeda_estrangeira":100.00,"numero_agencia_retirada":"7057"}'
# → 201
# {
#   "id_compra":1, "id_cliente":1, "cpf_cliente":"43488428095",
#   "dataSolicitacao":"2026-09-14T16:11:23.866",
#   "tipo_moeda":"EUR", "valor_moeda_estrangeira":100.00,
#   "valor_cotacao":6.5857, "valor_total_operacao":658.57,
#   "numero_agencia_retirada":"7057"
# }

# 4) consultar a ordem
curl -s http://localhost:8083/ordens/1

# 5) mexer na cotação e refazer a ordem — o total muda
curl -s -X PUT http://localhost:8082/cotacoes/EUR \
  -H 'Content-Type: application/json' -d '{"valorCotacao":7.0000}'
```

### Erros que valem demonstrar

```bash
# moeda fora do catálogo → 422
curl -i -X POST http://localhost:8083/ordens -H 'Content-Type: application/json' \
  -d '{"cpf_cliente":"43488428095","tipo_moeda":"JPY","valor_moeda_estrangeira":10,"numero_agencia_retirada":"7057"}'

# agência com 3 dígitos → 422
curl -i -X POST http://localhost:8083/ordens -H 'Content-Type: application/json' \
  -d '{"cpf_cliente":"43488428095","tipo_moeda":"USD","valor_moeda_estrangeira":10,"numero_agencia_retirada":"705"}'

# CPF com 3 dígitos → 400 (formato, não regra de negócio)
curl -i -X POST http://localhost:8083/ordens -H 'Content-Type: application/json' \
  -d '{"cpf_cliente":"123","tipo_moeda":"USD","valor_moeda_estrangeira":10,"numero_agencia_retirada":"7057"}'

# CPF válido mas não cadastrado → 404 (quem decidiu isso foi o cliente-service)
curl -i -X POST http://localhost:8083/ordens -H 'Content-Type: application/json' \
  -d '{"cpf_cliente":"00000000000","tipo_moeda":"USD","valor_moeda_estrangeira":10,"numero_agencia_retirada":"7057"}'
```

## 🔴 A demonstração da aula: derrube o cliente-service

Com os três no ar, mate o terminal 1 (`Ctrl+C`) e repita o passo 3:

```
HTTP/1.1 503 Service Unavailable
{
  "status": 503,
  "erro": "Servico indisponivel",
  "mensagem": "Um servico do qual dependemos nao respondeu (cliente-service na 8081
               ou cotacao-service na 8082). Confira se ambos estao no ar."
}
```

O que aconteceu, passo a passo:

1. `ClienteClient` tentou abrir conexão em `localhost:8081` → **connection refused**;
2. o `RestClient` embrulhou isso numa `ResourceAccessException`;
3. o `@RestControllerAdvice` traduziu para **503** com mensagem acionável.

**Três perguntas para a turma:**

- Por que **503** e não 500? (Não é bug nosso: é indisponibilidade temporária de terceiro. O cliente pode tentar de novo.)
- Sem `@ExceptionHandler`, o que o usuário veria? (500 com stack trace — vazamento de detalhe interno e mensagem inútil.)
- Por que **timeout de 3s** em `ClientesHttpConfig`? (Sem timeout, um vizinho *lento* é pior que um vizinho *morto*: ele segura nossas threads até o pool esgotar. Falha rápido é melhor que travar devagar.)

E a pergunta que abre a Aula 6: **quem escreveu `http://localhost:8081` no `application.yml` vai atualizar esse arquivo quando o serviço mudar de máquina?**

## O que este estado NÃO tem (de propósito)

- **Service discovery** — a URL é fixa. → Aula 6.
- **Retry / circuit breaker** — uma falha do vizinho é uma falha nossa, direto.
- **Transação distribuída** — se a ordem gravar e algo depois falhar, não há compensação. É consciente: a ordem é o último passo do fluxo.
- **Gateway** — o consumidor precisa conhecer as três portas.

## Testes

```bash
cd aula-05 && mvn clean test
```

| Módulo | Nível | Classes |
|---|---|---|
| `cliente-service` | unidade / fatia web / contexto | `ClienteServiceTest`, `ClienteControllerTest`, `ClienteServiceApplicationTest` |
| `cotacao-service` | unidade / adapter / fatia web / integração | `CotacaoServiceTest`, `MoedaTest`, `CotacaoLocalProviderTest`, `AwesomeApiAdapterTest`, `CotacaoControllerTest`, `CotacaoServiceApplicationTest` |
| `cambio-service` | unidade **com vizinhos mockados** / fatia web / contexto | `OrdemServiceTest`, `OrdemControllerTest`, `CambioServiceApplicationTest` |

`OrdemServiceTest` é o teste que muda de sentido nesta aula: `ClienteClient` e `CotacaoClient` viraram **mocks de fronteira de rede**. Nenhuma porta é aberta durante o `mvn test` — se fosse preciso subir três serviços para rodar um teste unitário, a quebra teria sido mal feita.
