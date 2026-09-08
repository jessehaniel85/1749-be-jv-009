# Padrões de projeto no câmbio — qual, onde e por quê

> **A regra que vale para os cinco:** todo padrão desta tabela entrou porque um problema
> concreto apareceu primeiro. Nenhum foi escolhido antes do problema. Padrão aplicado sem
> problema é decoração — e decoração custa manutenção.

## Tabela

| Padrão | Classe(s) | Problema que resolveu | Nasceu na |
|---|---|---|---|
| **Strategy** | `CalculoOperacaoStrategy` · `CalculoUsdStrategy` · `CalculoEurStrategy` · `EstrategiasDeCalculoConfig` | Cada moeda ganhou regra comercial própria (EUR com spread de 0,5%, USD sem). Sem ele: um `switch (moeda)` que cresce a cada moeda nova. | Aula 8 |
| **Facade** | `CambioFacade` | Registrar uma ordem envolve 5 colaboradores **numa ordem que importa**. Sem ela, o controller carregaria a coreografia — e todo novo ponto de entrada a repetiria. | Aula 8 |
| **Adapter** | `AwesomeApiAdapter` (cotacao-service) · `ClienteClientAdapter` · `CotacaoClientAdapter` (cambio-service) | Traduzir o formato **do outro** para o nosso — dado e erro — num único ponto. | Aula 4 (externo) e Aula 7 (internos); **nomeado** na Aula 8 |
| **Singleton** | `CatalogoMoedasBean` (usado) · `CatalogoMoedas` · `CatalogoMoedasEnum` (didáticos) | Uma única fonte de verdade sobre quais moedas existem. | Aula 8 |
| **Builder** | `OrdemResponse.Construtor` | 9 componentes, 3 `BigDecimal` vizinhos: trocar `valorCotacao` com `valorTotalOperacao` **compila**. | Aula 8 |
| **Template Method** (herdado do framework) | `JpaRepository` | — | usado, não escrito por nós |

---

## Strategy — a regra do cálculo, uma por moeda

### O `switch` que não escrevemos

```java
// ❌ o caminho fácil
public BigDecimal calcularTotal(Moeda moeda, BigDecimal valor, BigDecimal cotacao) {
    switch (moeda) {
        case USD: return valor.multiply(cotacao).setScale(2, HALF_EVEN);
        case EUR: return valor.multiply(cotacao).multiply(SPREAD).setScale(2, HALF_EVEN);
        default:  throw new MoedaNaoSuportadaException(moeda.name());
    }
}
```

Funciona hoje. O problema é o amanhã: cada moeda nova **edita um método que já estava certo**,
e o método vira uma escada. Pior — o mesmo `switch` tende a se reproduzir em outros pontos
(taxa, limite, horário de mesa), e agora são cinco escadas para manter em sincronia.

### O que temos

```java
public interface CalculoOperacaoStrategy {
    Moeda moeda();
    BigDecimal calcular(BigDecimal valorMoedaEstrangeira, BigDecimal cotacao);
}
```

```java
@Configuration
public class EstrategiasDeCalculoConfig {
    @Bean
    public Map<Moeda, CalculoOperacaoStrategy> estrategiasPorMoeda(
            List<CalculoOperacaoStrategy> estrategias) { ... }
}
```

O Spring injeta a `List` com **todas** as implementações do contexto; a `@Configuration`
indexa por moeda. **Adicionar GBP = criar `CalculoGbpStrategy` com `@Component`.** Nenhum
arquivo existente muda — nem a config, nem a calculadora, nem os testes.

Dois detalhes que valem a discussão em aula:

- **A config falha na subida** se duas estratégias reivindicarem a mesma moeda, ou se alguma
  moeda do enum ficar sem estratégia. Erro de fiação tem de aparecer no `mvn spring-boot:run`,
  não na primeira ordem em produção.
- **A ordem das operações no `CalculoEurStrategy`**: multiplica, aplica spread, **e só então**
  arredonda. Arredondar duas vezes introduz um centavo de erro em algumas faixas — e em
  sistema financeiro esse centavo aparece na conciliação.

> **Quando NÃO usar Strategy:** duas variações e nenhuma perspectiva de uma terceira. Aí o
> `if` é mais honesto que quatro arquivos. O Strategy paga quando o **eixo de variação é
> aberto** — e "uma moeda nova" é exatamente isso.

---

## Facade — uma porta de entrada para a operação

### Antes (Aula 7)

```java
public class OrdemController {
    public OrdemController(OrdemService ordemService) { ... }   // que fazia tudo por dentro
}
```

O `OrdemService` orquestrava **e** persistia. Duas razões para mudar na mesma classe.

### Depois (Aula 8)

```
OrdemController ──▶ CambioFacade ──┬──▶ ValidadorDeOrdem
                                   ├──▶ ConsultaCliente      (porta → Adapter → Feign)
                                   ├──▶ ConsultaCotacao      (porta → Adapter → Feign)
                                   ├──▶ CalculadoraDeOperacao ──▶ Strategy da moeda
                                   └──▶ OrdemService          (persistência)
```

O controller passou a ter **uma** dependência. E o `OrdemService` encolheu para o que sempre
foi a sua razão de existir: guardar e recuperar ordens.

**O que a Facade NÃO é:** uma classe que faz tudo. Ela não tem regra própria — se você
encontrar um cálculo ou uma validação dentro de uma Facade, ela virou o *God Object* que
deveria evitar. Ela conhece só a **coreografia**, inclusive a parte que importa: validar
antes de gastar chamada de rede.

Esse detalhe está testado em `CambioFacadeTest#moedaNaoSuportadaNaoChamaVizinhos`, com
`verifyNoInteractions(consultaCliente, consultaCotacao)`. É um teste de *ordem*, não de
resultado — e é o tipo de coisa que se quebra sem ninguém perceber quando não há teste.

---

## Adapter — a fronteira entre o nosso vocabulário e o dos outros

Três Adapters, três fronteiras:

| Adapter | Traduz | De | Para |
|---|---|---|---|
| `AwesomeApiAdapter` | JSON externo | `{"USDBRL":{"bid":"5.4321", ...}}` | `Cotacao` |
| `ClienteClientAdapter` | JSON + **erro** | `ClienteResumoJson`, `FeignException.NotFound` | `ClienteEncontrado`, `ClienteNaoEncontradoException` |
| `CotacaoClientAdapter` | JSON + **erro** | `CotacaoResumoJson`, `FeignException` | `CotacaoVigente`, `MoedaNaoSuportadaException` |

O ponto mais fácil de esquecer é o segundo: **o Adapter também traduz exceção**. Metade das
violações de fronteira que se vê em projeto real vaza pelo erro, não pelo tipo de retorno —
um `FeignException` atravessando três camadas até o `@RestControllerAdvice`.

Verificável na hora:

```bash
grep -rl "import feign" aula-08/cambio-service/src/main
# → infra/ClienteClientAdapter.java
# → infra/CotacaoClientAdapter.java
```

Dois arquivos. Trocar Feign por gRPC é reescrever esses dois.

---

## Singleton — e por que o do Spring é melhor que o do GoF

Estão as três formas no repositório, de propósito:

| | `CatalogoMoedas` | `CatalogoMoedasEnum` | `CatalogoMoedasBean` |
|---|---|---|---|
| Como | `getInstance()` + holder idiom | constante de `enum` | `@Component` |
| Thread-safe | sim (garantia da JVM na init de classe) | sim | sim |
| À prova de reflexão | **não** | sim | n/a |
| À prova de serialização | **não** | sim | n/a |
| **Substituível em teste** | **não** | **não** | **sim** |
| Usado no fluxo real | não | não | **sim** |

As duas primeiras existem como material didático — vale escrevê-las à mão uma vez para
entender o *holder idiom* e por que Joshua Bloch prefere o enum. **Mas a que roda é o bean**,
e o motivo é a linha em negrito.

```java
// com Singleton clássico, o colaborador é invisível e insubstituível
public class ValidadorDeOrdem {
    public Moeda moedaDe(String sigla) {
        return CatalogoMoedas.getInstance().resolver(sigla);   // ❌ como testar com outro catálogo?
    }
}

// com bean, a dependência é declarada e trocável
public class ValidadorDeOrdem {
    private final CatalogoDeMoedas catalogo;
    public ValidadorDeOrdem(CatalogoDeMoedas catalogo) { this.catalogo = catalogo; }   // ✅
}
```

O `getInstance()` chamado lá dentro é uma dependência **escondida**: não aparece no
construtor, não aparece na assinatura, e o teste não tem como substituí-la. É estado global
com roupa de padrão de projeto.

> **Para pensar:** se o escopo padrão do Spring já é singleton, o padrão Singleton do GoF
> morreu? *Não* — ele continua valendo onde não há container: bibliotecas, utilitários, código
> sem framework. O que mudou é que **dentro de uma aplicação Spring, escrever `getInstance()`
> à mão quase sempre é trabalho perdido com um efeito colateral ruim.**

---

## Builder — quando a ordem dos parâmetros é a única defesa

```java
// ❌ compila. E está errado: valorCotacao e valorTotalOperacao trocados.
new OrdemResponse(1L, 1L, "43488428095", agora, Moeda.EUR,
        new BigDecimal("100.00"),
        new BigDecimal("661.86"),      // deveria ser a cotação
        new BigDecimal("6.5857"),      // deveria ser o total
        "7057");
```

```java
// ✅ o nome no ponto da chamada torna o erro visível na leitura
OrdemResponse.construtor()
        .valorCotacao(new BigDecimal("6.5857"))
        .valorTotalOperacao(new BigDecimal("661.86"))
        .construir();
```

O Builder é **opcional** aqui — `OrdemResponse.de(ordem)` continua sendo o caminho do
controller. Ele entrou por um critério objetivo: **9 componentes, 3 do mesmo tipo e
adjacentes**. Abaixo de ~4 parâmetros, ou com tipos todos distintos, o construtor canônico do
record é melhor: menos código para manter.

---

## O que NÃO usamos, e por quê

| Padrão | Por que ficou de fora |
|---|---|
| **Observer** | Não há evento de domínio para publicar neste escopo. Entraria com naturalidade se a ordem precisasse notificar antifraude ou extrato — é o tema do módulo BE-JV-010 (mensageria). |
| **Decorator** | Seria a forma elegante de acrescentar cache ou retry ao `CotacaoProvider`. Não temos requisito de cache; incluí-lo agora seria abstração especulativa. |
| **Factory Method** | O Spring **é** a nossa fábrica. Escrever uma à mão duplicaria o container. |
| **Repository** | Já usamos — só que fornecido pelo Spring Data, não escrito por nós. |
| **Chain of Responsibility** | Cogitado para as validações. Com **duas** regras, a cadeia custa mais do que devolve. Se a validação crescer para seis ou sete, aí sim. |

Essa última coluna é a parte mais importante desta página. **A pergunta certa não é "qual
padrão eu uso?", é "que problema eu tenho?"** — e a resposta legítima, com frequência, é
"nenhum que este padrão resolva".
