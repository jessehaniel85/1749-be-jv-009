# Antes e depois — os cinco princípios, no nosso código

Todo trecho "antes" saiu de verdade de `aula-06/`. Todo trecho "depois" está em `aula-07/`.
**Nenhum status HTTP mudou** — os testes de controller têm as mesmas asserções nas duas aulas.

---

## 0. Injeção por construtor (o pré-requisito de tudo)

Este projeto **já usa injeção por construtor desde a Aula 1** — não havia `@Autowired` em campo
para remover. Guardamos aqui o **contra-exemplo**, porque é o que a turma vai encontrar em
código legado e precisa saber por que evitar.

### ❌ Contra-exemplo (nunca escreva assim)

```java
@Service
public class OrdemService {

    @Autowired                          // injeção em CAMPO
    private OrdemRepository repositorio;

    @Autowired
    private ClienteClient clienteClient;

    @Autowired
    private CotacaoClient cotacaoClient;

    // sem construtor: quem instancia esta classe recebe três nulls
}
```

Quatro problemas concretos, em ordem de dor:

1. **Não dá para testar sem Spring.** `new OrdemService()` devolve um objeto com três campos
   `null`. Ou você sobe um contexto, ou apela para reflexão (`ReflectionTestUtils`).
2. **Some a evidência de acoplamento.** Ninguém percebe que a classe cresceu para oito
   dependências — não há construtor gritando na cara. Um construtor com oito parâmetros é
   feio *de propósito*: a feiura é o alarme do SRP disparando.
3. **Campo não pode ser `final`.** Um `@Autowired` em campo permite que a dependência seja
   trocada em tempo de execução. Objeto que não é imutável em suas colaborações é objeto
   sobre o qual você não consegue raciocinar.
4. **Dependência opcional vira `NullPointerException` em produção**, não erro na subida.

### ✅ Como está (e sempre esteve) no projeto

```java
@Service
public class OrdemService {

    private final OrdemRepository repositorio;
    private final ConsultaCliente consultaCliente;
    private final ConsultaCotacao consultaCotacao;
    private final ValidadorDeOrdem validador;
    private final CalculadoraDeOperacao calculadora;

    public OrdemService(OrdemRepository repositorio,
                        ConsultaCliente consultaCliente,
                        ConsultaCotacao consultaCotacao,
                        ValidadorDeOrdem validador,
                        CalculadoraDeOperacao calculadora) {
        this.repositorio = repositorio;
        this.consultaCliente = consultaCliente;
        this.consultaCotacao = consultaCotacao;
        this.validador = validador;
        this.calculadora = calculadora;
    }
```

Tudo `final`, tudo obrigatório, tudo visível. `OrdemServiceTest` monta o objeto com `new`,
sem contexto Spring nenhum. E o Spring nem precisa do `@Autowired`: com **um único
construtor**, ele infere.

> **Pergunte à turma:** cinco parâmetros já é demais? *Talvez.* É a conta a pagar por ter
> quebrado o SRP em pedaços — e é uma conta honesta: cada parâmetro tem um nome que diz o que
> faz. Compare com a alternativa: uma classe de 120 linhas fazendo tudo.

---

## 1. SRP — Single Responsibility Principle

> Uma classe deve ter **uma única razão para mudar**.

### ❌ Antes (`aula-06`) — `OrdemService.registrar`

```java
@Transactional
public OrdemDeCompra registrar(String cpfCliente,
                               String siglaMoeda,
                               BigDecimal valorMoedaEstrangeira,
                               String numeroAgenciaRetirada) {

    Moeda moeda = Moeda.deSigla(siglaMoeda)                       // (1) validação
            .orElseThrow(() -> new MoedaNaoSuportadaException(siglaMoeda));

    if (numeroAgenciaRetirada == null || !numeroAgenciaRetirada.matches("\\d{4}")) {
        throw new AgenciaInvalidaException(numeroAgenciaRetirada); // (1) validação + número mágico
    }

    ClienteResumo cliente = clienteClient.buscarPorCpf(cpfCliente);   // (2) integração
    CotacaoResumo cotacao = cotacaoClient.consultar(moeda);           // (2) integração

    BigDecimal valorTotalOperacao = valorMoedaEstrangeira             // (3) cálculo
            .multiply(cotacao.valorCotacao())
            .setScale(2, RoundingMode.HALF_EVEN);                     //     + números mágicos

    OrdemDeCompra ordem = new OrdemDeCompra(cliente.id(), cliente.cpf(),
            LocalDateTime.now(), moeda, valorMoedaEstrangeira,
            cotacao.valorCotacao(), valorTotalOperacao, numeroAgenciaRetirada);

    return repositorio.save(ordem);                                   // (4) persistência
}
```

Quatro razões para este arquivo mudar: mudou a regra da agência, mudou o contrato do vizinho,
mudou o arredondamento, mudou o jeito de salvar. E **quatro níveis de abstração misturados**
numa função só — regra de negócio, chamada remota, aritmética e ORM, tudo no mesmo parágrafo.

### ✅ Depois (`aula-07`)

```java
@Transactional
public OrdemDeCompra registrar(NovaOrdem pedido) {
    Moeda moeda = validador.moedaDe(pedido.siglaMoeda());
    validador.validarAgencia(pedido.numeroAgenciaRetirada());

    ClienteEncontrado cliente = consultaCliente.porCpf(pedido.cpfCliente());
    CotacaoVigente cotacao = consultaCotacao.vigente(moeda);

    return repositorio.save(montarOrdem(pedido, moeda, cliente, cotacao));
}
```

Cinco linhas, **um nível de abstração**: cada uma delega a quem sabe fazer. Lê-se como o
enunciado da história US-05. A validação foi para `ValidadorDeOrdem`, o cálculo para
`CalculadoraDeOperacao`, e os números viraram `RegrasDeCambio.TAMANHO_AGENCIA` /
`ESCALA_MONETARIA` / `ARREDONDAMENTO`.

**O ganho aparece nos testes:** `ValidadorDeOrdemTest` e `CalculadoraDeOperacaoTest` não têm
mock nenhum, não têm Spring e rodam em milissegundos. Antes, testar o arredondamento exigia
montar dois mocks de cliente HTTP.

---

## 2. OCP — Open/Closed Principle

> Aberto para **extensão**, fechado para **modificação**.

### ❌ Antes (o que teria acontecido sem o contrato da Aula 4)

```java
public Cotacao obter(Moeda moeda) {
    if ("externo".equals(fonte)) {
        return buscarNaAwesomeApi(moeda);
    } else if ("banco-central".equals(fonte)) {   // fonte nova → EDITAR esta classe
        return buscarNoBacen(moeda);
    }
    return repositorio.findByMoeda(moeda).orElseThrow();
}
```

Cada fonte nova **modifica** um método que já funcionava — e reabre o risco em tudo que ele
já fazia. É o `if` que denuncia: ele pergunta *"que tipo de coisa é esta?"*, e essa pergunta
é trabalho do polimorfismo.

### ✅ Depois — `CotacaoProvider` (cotacao-service)

```java
public interface CotacaoProvider {
    Cotacao obter(Moeda moeda);
}

@Component
@ConditionalOnProperty(name = "cotacao.provedor", havingValue = "local", matchIfMissing = true)
public class CotacaoLocalProvider implements CotacaoProvider { ... }

@Component
@ConditionalOnProperty(name = "cotacao.provedor", havingValue = "externo")
public class CotacaoExternaProvider implements CotacaoProvider { ... }
```

Uma fonte nova é **um arquivo novo**. `CotacaoService`, `CotacaoController` e os testes
existentes não mudam nem uma vírgula. **O sinal de que o OCP está valendo: a palavra `if`
nunca aparece perguntando "qual provedor é este?"** — quem decide é o Spring, pela
propriedade.

---

## 3. LSP — Liskov Substitution Principle

> Toda implementação tem de **honrar o contrato da abstração**, sem surpresas.

O LSP não se cumpre com sintaxe — o compilador aceita qualquer implementação. Ele se cumpre
**escrevendo o contrato** e fazendo todas as implementações obedecerem.

### ❌ Violação clássica (o que NÃO fazemos)

```java
@Component
public class CotacaoExternaProvider implements CotacaoProvider {
    @Override
    public Cotacao obter(Moeda moeda) {
        try {
            return chamarApiExterna(moeda);
        } catch (Exception e) {
            return null;                    // ⚠️ compila. E destrói a abstração.
        }
    }
}
```

Compila, passa no code review desatento e quebra em produção. Pior: obriga **todo chamador**
a saber qual implementação está ativa para decidir se testa `null` — e nesse instante a
abstração deixou de existir.

Variantes da mesma violação: devolver `BigDecimal.ZERO` "para não quebrar"; lançar
`UnsupportedOperationException` em um método da interface; exigir uma pré-condição mais forte
que a declarada.

### ✅ Depois — contrato explícito no Javadoc de `CotacaoProvider`

```java
/**
 * <p><b>Pré-condições</b> (o chamador garante): moeda não nula, do enum Moeda.</p>
 * <p><b>Pós-condições</b> (a implementação garante):</p>
 * <ul>
 *   <li>devolve Cotacao não nula, com moeda igual à pedida;</li>
 *   <li>valorCotacao estritamente positivo, até ESCALA_COTACAO casas;</li>
 *   <li>dataHora nunca null;</li>
 *   <li>a instância PODE não estar persistida (o provedor externo devolve transiente);</li>
 *   <li>falha de fonte vira CotacaoIndisponivelException — e NUNCA null.</li>
 * </ul>
 */
Cotacao obter(Moeda moeda);
```

A quarta linha é a mais importante da lista: ela declara uma limitação *real* das
implementações (o provedor externo não persiste) **na abstração**, para que nenhum chamador
assuma `id` preenchido. Contrato honesto é contrato que documenta o pior caso.

---

## 4. ISP — Interface Segregation Principle

> Ninguém deve ser obrigado a depender de métodos que não usa.

### ❌ Antes (`aula-06`) — uma porta gorda para todo mundo

```java
// cliente-service
@Service
public class ClienteService {
    public Cliente cadastrar(Cliente cliente) { ... }
    public Cliente buscarPorCpf(String cpf)   { ... }
}

// e no controller, dependência da CLASSE inteira
private final ClienteService clienteService;
```

E no `cambio-service`, a interface Feign era o contrato genérico do vizinho — quem a
importasse enxergaria tudo o que o vizinho expõe, inclusive o que nunca vai usar.

### ✅ Depois — duas portas pequenas, cada consumidor com a sua

```java
// cliente-service/dominio
public interface ConsultaCliente { Cliente buscarPorCpf(String cpf); }
public interface CadastroCliente { Cliente cadastrar(Cliente cliente); }

@Service
public class ClienteService implements ConsultaCliente, CadastroCliente { ... }
```

```java
// cambio-service/dominio — só existe a porta de LEITURA
public interface ConsultaCliente {
    ClienteEncontrado porCpf(String cpf);
}
```

O ponto mais forte do ISP aqui é uma **ausência**: no `cambio-service` **não existe**
`CadastroCliente`. O câmbio nunca cadastra cliente, e a ausência da interface documenta isso
melhor do que qualquer comentário. No `cliente-service`, onde os dois usos existem de fato,
as duas interfaces existem.

> **Pergunte à turma:** e se amanhã o câmbio precisar cadastrar? Aí a interface nasce —
> quando o caso real aparecer, não antes. Criar `CadastroCliente` "por simetria" seria
> abstração especulativa: código morto com aparência de arquitetura.

---

## 5. DIP — Dependency Inversion Principle

> Módulos de alto nível não dependem de módulos de baixo nível. **Ambos dependem de abstrações.**

Este é o mais valioso dos cinco no nosso projeto, e o que muda o desenho de verdade.

### ❌ Antes (`aula-06`) — a seta apontando para fora

```java
package br.com.ada.cambio.ordem.dominio;

import br.com.ada.cambio.ordem.infra.ClienteClient;   // ⚠️ domínio → infra
import br.com.ada.cambio.ordem.infra.CotacaoClient;   // ⚠️
import br.com.ada.cambio.ordem.infra.CotacaoResumo;   // ⚠️

@Service
public class OrdemService { ... }
```

E o vazamento chegava até a borda:

```java
package br.com.ada.cambio.ordem.api;

import feign.FeignException;      // ⚠️ o @RestControllerAdvice conhecendo a biblioteca HTTP
import feign.RetryableException;  // ⚠️

@ExceptionHandler(FeignException.NotFound.class)
public ResponseEntity<ErroResposta> tratarNaoEncontradoNoVizinho(...) { ... }
```

Consequência prática: trocar Feign por gRPC (ou por `@HttpExchange`) mexeria em **três**
camadas. E os testes de `OrdemService` precisavam construir `FeignException` na mão.

### ✅ Depois — a seta apontando para dentro

```java
// dominio — define O QUE precisa
public interface ConsultaCliente { ClienteEncontrado porCpf(String cpf); }
public interface ConsultaCotacao { CotacaoVigente vigente(Moeda moeda); }
```

```java
// infra — implementa COMO faz, e depende do domínio
@Component
public class ClienteClientAdapter implements ConsultaCliente {

    private final ClienteFeignClient feignClient;

    @Override
    public ClienteEncontrado porCpf(String cpf) {
        try {
            return converter(feignClient.buscarPorCpf(cpf));
        } catch (FeignException.NotFound excecao) {
            throw new ClienteNaoEncontradoException(cpf);          // erro de domínio
        } catch (FeignException | IllegalStateException excecao) {
            throw new ServicoIndisponivelException("cliente-service", excecao);
        }
    }
}
```

```java
// api — só vocabulário de negócio; nenhum import de feign
@ExceptionHandler(ClienteNaoEncontradoException.class)
public ResponseEntity<ErroResposta> tratarClienteInexistente(...) { ... }
```

**O `ClienteClientAdapter` é o único arquivo do `cambio-service` que importa `feign`.**
Essa frase é o teste de mesa do DIP — e dá para verificar na hora, ao vivo:

```bash
grep -rl "import feign" aula-06/cambio-service/src/main
# → api/TratadorDeErros.java          <-- a lib de transporte chegou ate a BORDA

grep -rl "import feign" aula-07/cambio-service/src/main
# → infra/ClienteClientAdapter.java
# → infra/CotacaoClientAdapter.java   <-- so a infra, e so nos dois Adapters
```

Em numero de arquivos quase empata. O que mudou e **em qual camada** eles estao. Nos testes,
a mesma historia:

```bash
grep -rl "import feign" aula-06/cambio-service/src/test   # inclui dominio/OrdemServiceTest
grep -rl "import feign" aula-07/cambio-service/src/test   # so os testes dos dois Adapters
```

E a inversão vale também para o **erro**, não só para o dado: `FeignException.NotFound` (um
detalhe de HTTP) vira `ClienteNaoEncontradoException` (um fato do negócio) exatamente na
fronteira. Metade das violações de DIP que se vê em projeto real vaza pela exceção, não pelo
tipo de retorno.

---

## Resumo — o que mudou de arquivo para arquivo

| Princípio | Antes (aula-06) | Depois (aula-07) |
|---|---|---|
| Construtor | já era construtor | mantido; contra-exemplo documentado aqui |
| SRP | `OrdemService` com 4 responsabilidades | `ValidadorDeOrdem` + `CalculadoraDeOperacao` + `OrdemService` |
| OCP | `CotacaoProvider` (já vinha da Aula 4) | contrato reforçado, sem `if` de tipo |
| LSP | contrato implícito | pré/pós-condições no Javadoc da interface |
| ISP | `ClienteService` concreto | `ConsultaCliente` + `CadastroCliente` |
| DIP | `dominio` → `infra`, `api` → `feign` | `infra` → `dominio`; `feign` só nos Adapters |

**Nada disso mudou uma resposta HTTP.** É essa a definição de refatoração — e é por isso que
ela só é segura com a suíte de testes da Aula 2 no lugar.
