# Contrato do serviço de Cotação

**Versão:** 1.0 · **Dono:** domínio `cotacao` · **Consumidores:** domínio `ordem` (interno), app móvel (externo)

Este documento é o **contrato** do serviço de cotação: o que ele promete, o que aceita e o que devolve. É a peça que permite separar `cotacao` em um serviço próprio na Aula 5 sem quebrar ninguém — quem consome programa contra este texto, não contra a implementação.

> Escrito em markdown de propósito: um contrato precisa ser lido por gente (produto, QA, o time do app) antes de ser consumido por máquina. Um YAML de OpenAPI complementa; não substitui.

---

## 1. Conceito

Uma **cotação** é o valor de **1 unidade de moeda estrangeira em reais (BRL)**, válido no instante `dataHora`.

| Campo | Tipo | Regra |
|---|---|---|
| `moeda` | string enum | `USD` ou `EUR`. Nenhuma outra sigla é operada. |
| `valorCotacao` | número decimal | Sempre **4 casas** decimais. Positivo. |
| `dataHora` | ISO-8601 local | Instante em que a cotação foi apurada. |

**Por que 4 casas:** é a precisão com que o mercado publica câmbio. O arredondamento para 2 casas acontece **só no total da operação**, no domínio `ordem` — nunca na cotação. Arredondar cedo é como centavos somem em escala.

---

## 2. Operações

### 2.1 Consultar cotação vigente

```http
GET /cotacoes/{moeda}
```

| Parâmetro | Onde | Tipo | Obrigatório |
|---|---|---|---|
| `moeda` | path | string | sim |

**200 OK**

```json
{
  "moeda": "USD",
  "valorCotacao": 5.4321,
  "dataHora": "2026-09-14T09:00:00"
}
```

**422 Unprocessable Entity** — sigla não operada (ex.: `JPY`)

```json
{
  "status": 422,
  "mensagem": "Moeda não suportada: JPY. Moedas operadas: USD, EUR",
  "dataHora": "2026-09-14T09:00:01.482"
}
```

**503 Service Unavailable** — o provedor configurado não respondeu utilmente (só ocorre com `cotacao.provedor=externo`).

> **Nota de desenho:** a sigla desconhecida devolve **422**, não 400. A requisição está bem formada — o servidor entendeu perfeitamente o pedido; é o **negócio** que recusa. Por isso o path variable é recebido como `String` e convertido pelo domínio (`Moeda.paraSigla`), e não tipado como enum: se fosse enum, o Spring devolveria 400 antes de o domínio ser consultado.

### 2.2 Atualizar a cotação local

```http
PUT /cotacoes/{moeda}
Content-Type: application/json

{ "valorCotacao": 5.9000 }
```

**200 OK** — devolve a cotação atualizada, no mesmo formato do `GET`.

| Erro | Status |
|---|---|
| `valorCotacao` ausente ou ≤ 0 | `400` com a lista de campos |
| sigla não operada | `422` |

**Escopo:** esta operação altera a **tabela local**. Serve para simular variação de mercado sem depender de internet. Com `cotacao.provedor=externo`, a alteração é gravada mas a consulta continua vindo do provedor externo — o que é, ele próprio, um bom assunto de aula.

---

## 3. As duas implementações do contrato

O contrato interno é a interface `CotacaoProvider` (`br.com.ada.cambio.cotacao.dominio`):

```java
public interface CotacaoProvider {
    Cotacao obter(Moeda moeda);
}
```

A implementação ativa é escolhida por propriedade — **nenhuma classe de domínio muda**:

| `cotacao.provedor` | Implementação | Origem do dado | Precisa de internet |
|---|---|---|---|
| `local` **(padrão)** | `CotacaoLocalProvider` | tabela `cotacoes` no H2, semeada por `data.sql` | não |
| `externo` | `CotacaoExternaProvider` | `GET https://economia.awesomeapi.com.br/last/{moeda}-BRL` | sim |

```yaml
cotacao:
  provedor: local          # troque para "externo" fora da rede corporativa
  externo:
    url: https://economia.awesomeapi.com.br/last
    timeout-segundos: 3
```

### 3.1 O provedor externo e o Adapter

A awesomeapi devolve um modelo que **não é o nosso**:

```json
{
  "USDBRL": {
    "code": "USD",
    "codein": "BRL",
    "bid": "5.4321",
    "create_date": "2026-09-08 10:31:02"
  }
}
```

Três incompatibilidades, todas resolvidas no `AwesomeApiAdapter` e **em nenhum outro lugar**:

| Deles | Nosso | Tradução |
|---|---|---|
| resposta chaveada por `"USDBRL"` | objeto único | busca a chave `{moeda}BRL`; ausente → `503` |
| `"bid": "5.4321"` (string) | `BigDecimal` escala 4 | `new BigDecimal(bid).setScale(4, HALF_EVEN)`; ilegível → `503` |
| `"2026-09-08 10:31:02"` | `LocalDateTime` | formato `yyyy-MM-dd HH:mm:ss`; ilegível → assume "agora" |

Isso é o padrão **Adapter** funcionando como **Anti-Corruption Layer**: o formato do fornecedor não atravessa a fronteira do nosso domínio. Se trocarmos de provedor amanhã, reescreve-se **uma classe**.

**Timeout:** 3 segundos para conexão e para leitura. Chamada remota sem timeout é uma thread presa para sempre; é assim que um fornecedor lento derruba uma API inteira.

---

## 4. Compatibilidade

Mudanças **permitidas** sem nova versão do contrato:

- acrescentar campo **opcional** na resposta;
- acrescentar uma moeda ao enum;
- trocar a implementação do provedor.

Mudanças que **exigem** nova versão:

- remover ou renomear campo;
- mudar a escala de `valorCotacao`;
- transformar um `422` em `400` (ou vice-versa) — status faz parte do contrato.
