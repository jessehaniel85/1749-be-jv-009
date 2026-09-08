# Material do Aluno — Aula 7: Clean Code e SOLID, refatorando sem quebrar

> **Tempo de leitura:** ~16 min. Nosso Câmbio funciona. Três serviços registrados no Eureka, Feign conversando, `mvn test` verde, `POST /ordens` devolvendo `201` com o comprovante certo. E aí chega o requisito: *"ordem em dólar passa a ter spread de 1%; euro continua sem"*. Você abre o `OrdemService`, encontra um método de sessenta linhas que valida CPF, chama o cliente, chama a cotação, multiplica, arredonda e salva — e sente aquele frio na barriga de quem sabe que mexer ali pode quebrar outra coisa. Esta aula é sobre transformar esse frio na barriga em confiança: mudar a **estrutura** do código sem mudar o **comportamento**, com os testes da Aula 2 como rede de proteção.

---

## 1. O código que funciona e que ninguém quer tocar

Existe uma diferença enorme entre "o código funciona" e "o código está bom": o primeiro é sobre hoje, o segundo é sobre a **próxima mudança**. Nosso `OrdemService` da Aula 6 funciona — e tem quatro razões distintas para mudar:

1. mudou a **regra de validação** (agência passou a ter 5 dígitos);
2. mudou a **regra de cálculo** (spread por moeda);
3. mudou a **persistência** (a ordem agora guarda o canal de origem);
4. mudou a **integração** (saímos do Feign e fomos para outra coisa).

Quatro motivos, quatro times potencialmente diferentes, um único arquivo — e a cada mudança o risco de quebrar as outras três.

O ponto central do Clean Code é econômico, não estético: **código é lido muitas vezes mais do que é escrito**. Cada minuto economizado num nome enigmático alguém paga multiplicado em cada leitura — provavelmente você, daqui a três meses, às 22h, com um incidente aberto.

> **💭 Pare e reflita:** pense no sistema em que você trabalha hoje. Qual é o arquivo que ninguém quer abrir? O que exatamente faz dele "o arquivo assustador" — o tamanho, o número de responsabilidades, a falta de testes, ou o fato de você não conseguir prever o que quebra quando mexe?

---

## 2. Refatorar é mudar a estrutura sem mudar o comportamento

A definição é do Martin Fowler e cabe numa linha: **refatoração é uma alteração na estrutura interna do software para torná-lo mais fácil de entender e mais barato de modificar, sem alterar seu comportamento observável.**

As duas metades importam. "Mais barato de modificar" é o objetivo; "sem alterar o comportamento observável" é a restrição — e é o que separa refatoração de reescrita. Se depois da sua mudança o `POST /ordens` devolve um JSON diferente, você não refatorou: mudou o sistema. Pode até ser certo, mas é outra coisa, com outro risco e outro combinado com o PO.

Como você **sabe** que o comportamento não mudou? Você não sabe: seus testes sabem. É por isso que a Aula 2 veio antes desta. Os testes unitários, o `@WebMvcTest` e o teste de integração que escrevemos são exatamente a rede que permite mexer na estrutura hoje sem medo.

O método de hoje, que vale levar para o trabalho:

```
verde → uma mudança pequena → verde → commit → repete
```

Uma mudança por vez. Passou? Commit. Se quebrou, você sabe **exatamente** o que quebrou, porque a última mudança foi pequena. Refatoração de três horas, quarenta arquivos e nenhum commit no meio é a receita do "desisto, `git checkout .`".

> **🎬 Imagine o cenário:** você recebe um sistema legado sem nenhum teste e precisa mudar a regra de arredondamento. Refatorar no escuro é apostar. O caminho profissional é escrever primeiro um **teste de caracterização**: um teste que simplesmente registra o que o sistema faz hoje — inclusive comportamentos estranhos que você não entende. Ele não diz que o comportamento é certo; diz que ele é **este**. Depois de tê-lo, você refatora com rede.

---

## 3. Clean Code em cinco hábitos

Estes cinco cobrem quase todo o ganho prático. Não são regras a decorar: são hábitos a instalar.

**1. Nomes revelam intenção.** Um nome bom responde "por que isso existe?" sem exigir que você leia o corpo.

```java
// antes
public Ordem exec(String c, String m, BigDecimal v, String a) { ... }

// depois
public Ordem registrarOrdemDeCompra(String cpfCliente,
                                    Moeda moeda,
                                    BigDecimal valorMoedaEstrangeira,
                                    String numeroAgenciaRetirada) { ... }
```

Note que o "depois" não tem comentário — ele não precisa. O nome é o comentário que nunca fica desatualizado.

**2. Funções pequenas e de um único nível de abstração.** O hábito mais subestimado. Um método não mistura *o que* fazer com *como* fazer: se uma linha diz `validar(nova)` e a seguinte faz `if (cpf.length() != 11)`, você pulou de andar no meio da frase, e quem lê reconstrói o contexto a cada linha.

```java
// depois: todas as linhas no mesmo nível de abstração
public OrdemResponse registrar(NovaOrdem nova) {
    validador.validar(nova);
    Cliente cliente = consultaCliente.buscarPorCpf(nova.cpfCliente());
    Cotacao cotacao = cotacaoProvider.buscar(nova.moeda());
    Ordem ordem = calculadora.calcular(nova, cliente, cotacao);
    return OrdemResponse.de(ordemRepository.save(ordem));
}
```

Cinco linhas contam a história inteira. Quem quiser o detalhe desce um andar.

**3. Constantes em vez de números mágicos.** `11`, `4` e `2` espalhados pelo código são armadilhas: ninguém sabe se aquele `2` é escala monetária, número de tentativas ou tamanho de sufixo.

```java
public final class RegrasCambio {

    public static final int TAMANHO_CPF = 11;
    public static final int TAMANHO_AGENCIA = 4;
    public static final int ESCALA_MONETARIA = 2;
    public static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;

    private RegrasCambio() { }   // classe de constantes não se instancia
}
```

O ganho não é "ficou bonito": é que quando a agência virar 5 dígitos, existe **um** lugar para mudar, e o compilador encontra todos os usos.

**4. Comentários que mentem.** O comentário não é compilado, não é testado e não quebra o build quando fica errado — por isso ele envelhece pior que o código. A regra prática: se você sente vontade de escrever `// valida o cpf`, o que o código está pedindo é um método chamado `validarCpf`. Comentário bom explica **por quê**, não **o quê**:

```java
// HALF_EVEN (arredondamento bancário) exigido pela área de compliance:
// distribui o viés de arredondamento e evita ganho sistemático da instituição.
private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;
```

**5. Organização da classe.** Ordem previsível: constantes, campos, construtor, métodos públicos e, logo abaixo de quem os usa, os privados. A classe se lê como jornal: manchete em cima, detalhe embaixo.

> **✍️ Experimente agora:** abra o `OrdemService` do seu gabarito da Aula 6 e conte três coisas: (a) quantas linhas tem o maior método; (b) quantos literais numéricos aparecem; (c) quantos níveis de abstração diferentes existem dentro do método público. Anote os três números — no fim da aula você compara.

---

## 4. SRP e OCP: revisão rápida no `OrdemService`

Você já viu estes dois na trilha; a revisão aqui é curta e serve de degrau para o que vem.

**SRP — Single Responsibility Principle.** Uma classe deve ter **uma única razão para mudar**. O erro comum é ler isso como "fazer só uma coisa" e criar quarenta classes de cinco linhas. Não é sobre tamanho, é sobre **fontes de mudança**: se validação e cálculo mudam sempre juntos e pelo mesmo pedido, podem morar juntos; se mudam por pedidos de áreas diferentes, separe.

No nosso caso, separam-se naturalmente em três colaboradores:

| Responsabilidade | Quem passa a fazer | Muda quando |
|---|---|---|
| Validar a entrada | `OrdemValidator` | mudam as regras de formato (CPF, agência) |
| Calcular a operação | `CalculadoraOperacao` | muda a regra financeira (escala, spread) |
| Persistir | `OrdemRepository` | muda o modelo de dados |
| Orquestrar os três | `OrdemService` | muda o **fluxo** do caso de uso |

<details><summary>Ver esquema em texto…</summary>

```
ANTES — uma classe, quatro razões para mudar
   ┌──────────────────────────────────────────────┐
   │ OrdemService                                 │
   │   valida CPF e agência        ← regra de formato
   │   busca cliente no repositório← acesso a dados
   │   chama o Feign               ← protocolo
   │   multiplica e arredonda      ← regra financeira
   │   monta e salva a entidade    ← persistência
   └──────────────────────────────────────────────┘

DEPOIS — cada razão de mudança em seu lugar
   OrdemService (orquestra o caso de uso)
        ├──► OrdemValidator          (formato)
        ├──► ConsultaCliente         (leitura de cliente)
        ├──► CotacaoProvider         (cotação, sem saber de HTTP)
        ├──► CalculadoraOperacao     (regra financeira)
        └──► OrdemRepository         (persistência)
```
</details>

**OCP — Open/Closed Principle.** Aberto para extensão, fechado para modificação: dá para **acrescentar** comportamento sem **editar** o que já funciona e já está testado. É o princípio que responde ao spread por moeda — e a resposta completa é a Aula 8. Guarde a intuição: se adicionar um caso novo obriga a abrir um `if/else` existente, o OCP foi violado.

---

## 5. LSP: o princípio que fala de contratos

O Liskov Substitution Principle (Barbara Liskov, 1987) é o mais mal explicado dos cinco: quase sempre aparece como "subclasse pode substituir a superclasse" — verdade inútil. A formulação que serve é:

> **Quem usa uma abstração não pode ser surpreendido pela implementação que recebeu.**

Se o seu código funciona com `CotacaoProvider` e passa a receber outra implementação, nada pode quebrar. Vale para `extends` **e** para `implements`.

### O clássico Retângulo/Quadrado

```java
class Retangulo {
    protected int largura, altura;
    void setLargura(int l) { this.largura = l; }
    void setAltura(int a)  { this.altura = a; }
    int area() { return largura * altura; }
}

class Quadrado extends Retangulo {
    @Override void setLargura(int l) { this.largura = l; this.altura = l; }
    @Override void setAltura(int a)  { this.largura = a; this.altura = a; }
}
```

Matematicamente todo quadrado é um retângulo. No código, não: quem usa `Retangulo` espera que mudar a largura **não** mude a altura.

```java
Retangulo r = new Quadrado();
r.setLargura(5);
r.setAltura(4);
assertEquals(20, r.area());   // falha: devolve 16
```

O compilador aceitou tudo. Mas o **contrato implícito** ("largura e altura são independentes") foi quebrado e o cliente passou a errar. Herança que aceita o "é um" da linguagem natural sem verificar o comportamento esperado é a origem de metade dos bugs de LSP.

### O caso do nosso domínio: `CotacaoProvider`

Nossa interface é enganosamente simples:

```java
public interface CotacaoProvider {
    Cotacao buscar(Moeda moeda);
}
```

Mas ela carrega um **contrato**, que precisa estar escrito em algum lugar (Javadoc, teste, ou os dois):

- **Pré-condição** (o que o chamador garante): `moeda` não nula e pertencente ao catálogo suportado.
- **Pós-condição** (o que a implementação garante): devolve `Cotacao` **não nula**, com a mesma moeda pedida, `valorCotacao` positivo e `dataHora` preenchida.
- **Falha esperada:** `MoedaNaoSuportadaException` — e nenhuma outra exceção vazando do transporte.

Agora as três formas clássicas de violar isso, todas sem herança:

| Violação | Como aparece no código | Por que quebra o LSP |
|---|---|---|
| Devolve `null` quando o provedor externo falha | `catch (Exception e) { return null; }` | **enfraquece a pós-condição**: o chamador foi programado para nunca receber nulo |
| Deixa vazar `FeignException` / `RestClientException` | sem `try/catch`, exceção de transporte sobe | **falha inesperada**: o chamador só conhece `MoedaNaoSuportadaException` |
| Só funciona se `PUT /cotacoes/{moeda}` já tiver sido chamado | lança `IllegalStateException` se a tabela estiver vazia | **fortalece a pré-condição**: exige do chamador algo que a interface não pede |

A regra de ouro que resume as três: uma implementação pode exigir **menos** e prometer **mais** do que o contrato; nunca o contrário.

### Como transformar LSP em algo verificável

Contrato que só existe em conversa não sobrevive à próxima sprint. Escreva um **teste de contrato compartilhado** e faça toda implementação herdá-lo:

```java
abstract class CotacaoProviderContractTest {

    protected abstract CotacaoProvider provider();

    @ParameterizedTest
    @EnumSource(Moeda.class)
    void deveHonrarOContratoParaTodaMoedaSuportada(Moeda moeda) {
        Cotacao cotacao = provider().buscar(moeda);

        assertThat(cotacao).isNotNull();
        assertThat(cotacao.moeda()).isEqualTo(moeda);
        assertThat(cotacao.valorCotacao()).isPositive();
        assertThat(cotacao.dataHora()).isNotNull();
    }
}

class CotacaoLocalProviderTest extends CotacaoProviderContractTest {
    @Override protected CotacaoProvider provider() { return new CotacaoLocalProvider(repositorioFake()); }
}

class CotacaoRemotaProviderTest extends CotacaoProviderContractTest {
    @Override protected CotacaoProvider provider() { return new CotacaoRemotaProvider(feignFake()); }
}
```

A partir daqui, **toda nova implementação** de `CotacaoProvider` só entra se passar no mesmo teste: LSP deixou de ser princípio de slide e virou build vermelho.

---

## 6. ISP: interfaces pequenas, clientes livres

O Interface Segregation Principle diz que **nenhum cliente deve ser forçado a depender de métodos que não usa**. O exemplo canônico do módulo é `Veiculo` com `ligar()` e `abastecer()`, que obriga a `BicicletaEletrica` a implementar um `abastecer()` inexistente no mundo real — e o remédio é quebrar em duas interfaces.

No Câmbio, o cliente tem duas capacidades bem distintas:

```java
public interface ConsultaCliente {
    Cliente buscarPorCpf(String cpf);
}

public interface CadastroCliente {
    Cliente cadastrar(NovoCliente novo);
}

@Service
public class ClienteService implements ConsultaCliente, CadastroCliente {
    // uma classe pode implementar as duas; quem consome escolhe o que vê
}
```

O `OrdemService` **nunca** cadastra ninguém — ele só consulta. Então ele depende de `ConsultaCliente`, e só:

```java
public class OrdemService {
    private final ConsultaCliente consultaCliente;   // não conhece cadastro
    // ...
}
```

Três ganhos concretos, nenhum estético:

- **Teste trivial.** Um dublê de `ConsultaCliente` tem um método; um de "ClienteService inteiro" tem todos.
- **A dependência conta a verdade.** A assinatura do construtor já diz que ordens **só leem** clientes.
- **Muda menos.** Quando o cadastro ganhar `atualizarEndereco`, o `OrdemService` sequer toma conhecimento.

> **💭 Pare e reflita:** quantas interfaces do seu sistema têm mais de sete métodos? Quantos clientes dessas interfaces usam todos? A distância entre esses dois números é exatamente o acoplamento que o ISP remove.

---

## 7. DIP: dependa de abstrações — e injete pelo construtor

O Dependency Inversion Principle tem duas metades, e a segunda costuma ser esquecida: módulos de alto nível não devem depender dos de baixo nível (ambos dependem de abstrações), **e** abstrações não dependem de detalhes — detalhes é que dependem de abstrações.

No nosso código: a regra de registrar uma ordem **não pode saber** que a cotação vem por HTTP via Feign. Isso é transporte, e transporte muda (já mudou: `RestClient` na Aula 5, Feign na Aula 6).

<details><summary>Ver esquema em texto…</summary>

```
ANTES — a regra conhece o transporte
   OrdemService ───► CotacaoFeignClient ───► HTTP ───► cotacao-service
        (regra de negócio depende de detalhe de infraestrutura;
         trocar o Feign obriga a mexer na regra e nos testes dela)

DEPOIS — a regra conhece só o contrato
   OrdemService ───► «interface» CotacaoProvider
                              ▲
                              │ implementa
                     CotacaoRemotaProvider ───► CotacaoFeignClient ───► HTTP
                     CotacaoLocalProvider  ───► tabela H2

   A seta de dependência do detalhe agora aponta PARA a abstração:
   é isso que a palavra "inversão" quer dizer.
```
</details>

```java
@Component
class CotacaoRemotaProvider implements CotacaoProvider {

    private final CotacaoFeignClient feign;

    CotacaoRemotaProvider(CotacaoFeignClient feign) {
        this.feign = feign;
    }

    @Override
    public Cotacao buscar(Moeda moeda) {
        CotacaoRemotaResponse resposta = feign.buscarCotacao(moeda.name());
        return new Cotacao(moeda, resposta.valorCotacao(), resposta.dataHora());
    }
}
```

### Injeção por construtor × injeção por campo

Aqui mora a recomendação mais prática da aula. Compare:

```java
// ❌ injeção por campo
@Service
public class OrdemService {
    @Autowired private ConsultaCliente consultaCliente;
    @Autowired private CotacaoProvider cotacaoProvider;
    @Autowired private OrdemRepository ordemRepository;
}

// ✅ injeção por construtor (o Spring dispensa @Autowired em construtor único)
@Service
public class OrdemService {

    private final ConsultaCliente consultaCliente;
    private final CotacaoProvider cotacaoProvider;
    private final OrdemRepository ordemRepository;

    public OrdemService(ConsultaCliente consultaCliente,
                        CotacaoProvider cotacaoProvider,
                        OrdemRepository ordemRepository) {
        this.consultaCliente = consultaCliente;
        this.cotacaoProvider = cotacaoProvider;
        this.ordemRepository = ordemRepository;
    }
}
```

| Aspecto | `@Autowired` em campo | Injeção por construtor |
|---|---|---|
| Testar sem Spring | precisa de reflexão, `@InjectMocks` ou subir o contexto | `new OrdemService(mock, mock, mock)` |
| Imutabilidade | campo não pode ser `final` | campos `final`, objeto nasce pronto |
| Dependências visíveis | escondidas no meio da classe | explícitas na assinatura |
| Classe inchando | 12 campos passam despercebidos | construtor de 12 parâmetros **incomoda** — e deve |
| Objeto meio-construído | `new OrdemService()` compila e dá `NullPointerException` | impossível: ou constrói completo ou não constrói |
| Dependência circular | descoberta tarde, em runtime | estoura na subida, com mensagem clara |

O artigo de Marc Nuri (`blog.marcnuri.com/field-injection-is-not-recommended`), citado pelo planejamento oficial do módulo, sistematiza esses pontos. A linha que vale memorizar é a quarta: o construtor não impede a classe de crescer — ele **faz doer**, e essa dor é o feedback de design que o campo silencia.

> **✍️ Experimente agora:** escreva um teste do `OrdemService` sem nenhuma anotação de Spring: `new OrdemService(cpf -> clienteFake, moeda -> cotacaoFake, repositorioFake)`. Se isso compilar e rodar em milissegundos, você aplicou DIP e ISP de verdade. Se você precisar de `@SpringBootTest` para testar uma multiplicação, algo ainda está acoplado.

---

## 8. O `OrdemService` antes e depois (e as armadilhas)

**Antes** (fim da Aula 6 — funciona, e é difícil de mudar):

```java
@Service
public class OrdemService {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private CotacaoFeignClient cotacaoFeignClient;
    @Autowired private OrdemRepository ordemRepository;

    public Ordem criar(NovaOrdem nova) {
        if (nova.cpfCliente() == null || nova.cpfCliente().length() != 11) {
            throw new IllegalArgumentException("cpf inválido");
        }
        if (nova.numeroAgenciaRetirada().length() != 4) {
            throw new IllegalArgumentException("agência inválida");
        }
        Cliente cliente = clienteRepository.findByCpf(nova.cpfCliente())
                .orElseThrow(() -> new ClienteNaoEncontradoException(nova.cpfCliente()));
        var resposta = cotacaoFeignClient.buscarCotacao(nova.moeda().name());
        BigDecimal total = nova.valorMoedaEstrangeira()
                .multiply(resposta.valorCotacao())
                .setScale(2, RoundingMode.HALF_EVEN);
        Ordem ordem = new Ordem();
        ordem.setCliente(cliente);
        ordem.setMoeda(nova.moeda());
        ordem.setValorMoedaEstrangeira(nova.valorMoedaEstrangeira());
        ordem.setValorCotacao(resposta.valorCotacao());
        ordem.setValorTotalOperacao(total);
        ordem.setNumeroAgenciaRetirada(nova.numeroAgenciaRetirada());
        ordem.setDataSolicitacao(LocalDateTime.now());
        return ordemRepository.save(ordem);
    }
}
```

**Depois** (mesma resposta HTTP, mesmos testes verdes):

```java
@Service
public class OrdemService {

    private final OrdemValidator validador;
    private final ConsultaCliente consultaCliente;
    private final CotacaoProvider cotacaoProvider;
    private final CalculadoraOperacao calculadora;
    private final OrdemRepository ordemRepository;

    public OrdemService(OrdemValidator validador,
                        ConsultaCliente consultaCliente,
                        CotacaoProvider cotacaoProvider,
                        CalculadoraOperacao calculadora,
                        OrdemRepository ordemRepository) {
        this.validador = validador;
        this.consultaCliente = consultaCliente;
        this.cotacaoProvider = cotacaoProvider;
        this.calculadora = calculadora;
        this.ordemRepository = ordemRepository;
    }

    public Ordem registrar(NovaOrdem nova) {
        validador.validar(nova);
        Cliente cliente = consultaCliente.buscarPorCpf(nova.cpfCliente());
        Cotacao cotacao = cotacaoProvider.buscar(nova.moeda());
        Ordem ordem = calculadora.montarOrdem(nova, cliente, cotacao);
        return ordemRepository.save(ordem);
    }
}
```

Cinco linhas no método público, dependências explícitas, zero literais numéricos, zero conhecimento de HTTP. E o spread por moeda, que abriu a aula, agora tem **um** lugar óbvio para entrar: a `CalculadoraOperacao` — que na próxima aula vira uma família de estratégias.

**Armadilhas que valem mais que a teoria:**

- **Refatorar sem teste.** Sem rede você não está refatorando; está torcendo.
- **Misturar refatoração e mudança de comportamento no mesmo commit.** Quando o teste quebra, você não sabe se foi a estrutura ou a regra.
- **Interface para tudo.** Interface com uma implementação e nenhuma intenção de troca é só indireção. Interface é abstração de **intenção**, não taxa de entrada.
- **SRP como dogma.** Trinta classes de cinco linhas não são mais legíveis que cinco coesas.
- **Comentário para explicar código ruim.** Se o comentário é necessário para entender o `if`, melhore o `if`.

---

## 9. Ponte com o legado

Você provavelmente reconhece três destes no sistema que mantém hoje:

- **`new` dentro do service.** `ClienteDAO dao = new ClienteDAO();` no meio da regra amarra a regra ao acesso a dados: não dá para testar sem banco nem trocar a fonte. É a violação mais pura de DIP.
- **Lookup de dependência no meio da regra.** Em EJB clássico é o `InitialContext().lookup(...)`; em código recente, `ApplicationContext.getBean(...)`. Nos dois casos a classe **vai buscar** a dependência em vez de **recebê-la** — e não se instancia em teste sem o servidor inteiro.
- **`if/else` de tipo.** `if (tipo.equals("A")) … else if (tipo.equals("B")) …` crescendo a cada produto novo é OCP violado em estado puro — e é exatamente o que a Aula 8 resolve com Strategy.
- **God class.** O `ServicoGeral` de 3 000 linhas com 40 métodos públicos não nasceu assim: nasceu com 200 linhas e uma responsabilidade, e ninguém disse "não" nas 60 vezes seguintes.
- **`@Autowired` em campo em serviço já modernizado.** Muito código migrado copiou o estilo do tutorial de 2013. Trocar por construtor é refatoração mecânica, segura e de altíssimo retorno — comece por aí.

O veterano de legado tem uma vantagem real aqui: ele já **pagou** os juros da dívida técnica. Quando alguém diz "isso vai ficar caro de manter", ele não precisa acreditar — ele lembra.

---

## 10. IA & agentes hoje

**Código limpo é código que o agente edita sem quebrar.** Um método de 200 linhas com seis responsabilidades força o modelo a reescrever o bloco inteiro para mudar uma linha — e revisar esse diff é impraticável. Métodos curtos e bem nomeados produzem mudanças **localizadas**, revisáveis em trinta segundos. Estruturar bem o código virou também interface com a ferramenta.

**SOLID como guia de prompt de refatoração.** Peça a mudança com a restrição junto:

> *"Separe este método em validação, cálculo e persistência. Não altere o comportamento observável da API. Mantenha os testes existentes passando. Use injeção por construtor."*

Restrição explícita + critério de aceite verificável (`mvn test`) transforma sugestão em trabalho aproveitável.

**Revise o que o assistente gerou com os cinco princípios.** Checklist de trinta segundos: (1) esta classe ganhou mais de uma razão para mudar? (2) o próximo caso vai me obrigar a editar este `if`? (3) alguma implementação passou a devolver `null` ou lançar exceção nova? (4) alguma interface cresceu além do que o cliente usa? (5) a regra passou a conhecer o transporte? E um aviso: modelos ainda sugerem `@Autowired` em campo, porque foi o estilo dominante no código público por uma década. **Você é o filtro** — o modelo reproduz a média da internet, não a decisão do seu time.

**E cuidado com a rede falsa.** "Os testes passaram" só vale o que os testes valem: uma suíte que cobre só o caminho feliz aprova refatoração errada com a mesma barra verde.

---

## 11. Para ir além

- **Robert C. Martin**, *Código Limpo* — capítulos 2 (nomes) e 3 (funções): 40 páginas que mudam o jeito de escrever.
- **Robert C. Martin**, *Arquitetura Limpa* — parte III, um capítulo curto por princípio SOLID.
- **Martin Fowler**, *Refactoring* (2ª ed.) — o catálogo: *Extract Function*, *Introduce Parameter Object*, *Replace Magic Literal with Symbolic Constant*.
- **Marc Nuri**, *Field injection is not recommended* — `https://blog.marcnuri.com/field-injection-is-not-recommended`, a referência oficial do módulo para a discussão de construtor × campo.
- **Robert C. Martin**, *SOLID Relevance* (blog.cleancoder.com, 2020) — o próprio autor revisitando por que os princípios continuam válidos fora da OO clássica.

> **Na próxima aula (Aula 8 — Design Patterns):** ficou faltando o spread do dólar. Resolver com `if (moeda == USD)` funciona hoje e nos condena quando entrarem GBP e JPY. Existe um nome para a solução certa — e ele está num catálogo publicado em 1994 que a comunidade inteira já conhece. Vamos ver os três grupos de padrões, implementar **Strategy**, **Facade** e **Singleton** no Câmbio, dar nome ao **Adapter** que já usamos desde a Aula 4 — e discutir, com honestidade, quando um padrão é a solução e quando é só enfeite caro.
