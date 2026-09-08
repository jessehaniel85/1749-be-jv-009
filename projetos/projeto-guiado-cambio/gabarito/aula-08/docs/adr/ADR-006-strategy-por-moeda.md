# ADR-006 — Strategy para o cálculo da operação, uma por moeda

- **Status:** aceita
- **Data:** Aula 8
- **Decisores:** time do projeto guiado (turma 1749)
- **Depende de:** ADR-005 (SRP: o cálculo já estava isolado em `CalculadoraDeOperacao`)

## Contexto

Até a Aula 7 havia **uma** fórmula para todas as moedas:

```java
valorMoedaEstrangeira.multiply(valorCotacao)
        .setScale(ESCALA_MONETARIA, ARREDONDAMENTO);
```

A área comercial trouxe uma regra nova: o **euro** tem volume menor e custo de funding maior
para a mesa, e passa a ter **spread de 0,5%**. O **dólar**, moeda de maior giro, continua com
repasse da cotação cheia.

Não é um caso isolado — é o **primeiro** de uma série previsível: libra, iene, spread por
faixa de valor, spread por canal. O eixo "regra comercial por moeda" está aberto.

## Decisão

Interface `CalculoOperacaoStrategy` com `Moeda moeda()` e
`BigDecimal calcular(BigDecimal valorMoedaEstrangeira, BigDecimal cotacao)`.
Duas implementações `@Component` (`CalculoUsdStrategy`, `CalculoEurStrategy`), registradas
num `Map<Moeda, CalculoOperacaoStrategy>` que a `EstrategiasDeCalculoConfig` monta a partir
da `List<CalculoOperacaoStrategy>` injetada pelo Spring.

`CalculadoraDeOperacao` deixa de conter a fórmula e passa a **escolher** a estratégia.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| **`switch (moeda)` na calculadora** | Cada moeda nova edita um método que já funcionava (viola OCP), e o método vira escada. Pior: o mesmo `switch` tende a se replicar em outros pontos (taxa, limite, horário de mesa) e as cópias saem de sincronia. |
| **Método na própria enum `Moeda`** | Tentador e compacto. Descartado por dois motivos: (1) colocaria regra **comercial** dentro de um enum de **catálogo**, que é usado também pelo `cotacao-service`; (2) uma enum não tem como receber dependências — quando o spread vier de tabela ou de configuração, a regra não cabe mais lá. |
| **Spread como propriedade no `application.yml`** | Resolveria *este* caso (um número por moeda) e nenhum dos próximos: a regra do euro pode virar "spread por faixa de valor", que não é um número. Configuração cobre parâmetros; Strategy cobre **algoritmos**. |
| **Herança (`CalculoBase` com hook)** | Template Method acopla as implementações a um esqueleto comum que ainda não sabemos que existe. Composição primeiro; se três estratégias repetirem o mesmo esqueleto, aí extrai. |

## Consequências

**Ganhamos:**

- Moeda nova = **um arquivo novo** com `@Component`. Nenhum arquivo existente muda —
  nem a config, nem a calculadora, nem os testes que já passam.
- Cada regra comercial tem **nome próprio e teste próprio** (`CalculoEurStrategyTest` diz, em
  três asserções, o que a mesa cobra pelo euro). Auditoria abre um arquivo, não um service.
- Fiação errada falha **na subida**: a config recusa duas estratégias para a mesma moeda e
  recusa moeda do enum sem estratégia. Erro de configuração em produção é o pior lugar para
  descobrir que faltou um `@Component`.

**Pagamos:**

- **Mais arquivos.** Quatro (interface + duas estratégias + config) onde havia um método de
  três linhas. Vale porque o eixo de variação é real e aberto; num cálculo estável, seria
  cerimônia.
- **A regra fica menos "visível de uma vez".** Quem quiser ver todas as fórmulas lado a lado
  abre dois arquivos. Mitigado pelo `docs/patterns-no-projeto.md` e pelos nomes.
- **⚠️ Mudança de comportamento.** É a **única** de todo o gabarito desde a Aula 5: o total de
  uma ordem em EUR passou de `658.57` para `661.86`. Isso não contradiz a Aula 7 — lá era
  **refatoração** (comportamento idêntico por definição); aqui é **regra de negócio nova**,
  entregue como US-10, com teste que a documenta. A distinção entre as duas coisas é, em si,
  conteúdo da aula.

## Nota para quem for revisar o cálculo

A ordem das operações no `CalculoEurStrategy` é deliberada: **multiplica, aplica o spread e
só então arredonda.** Arredondar antes do spread introduz erro de um centavo em algumas
faixas de valor. Em sistema financeiro esse centavo não some — ele aparece na conciliação do
fim do dia, multiplicado pelo volume.
