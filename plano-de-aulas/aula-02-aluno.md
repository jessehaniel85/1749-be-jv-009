# Material do Aluno — Aula 2: Qualidade, pirâmide de testes e o primeiro olhar de arquiteto

> **Tempo de leitura:** ~15 min. Ontem o `POST /clientes` da API de Câmbio passou a responder 201. Hoje eu vou te pedir uma coisa banal: **mude o `ClienteService`** — CPF precisa ser validado, e cadastro duplicado precisa ser recusado. A pergunta que abre a aula é essa: *como você prova, para você mesmo e para quem audita, que o cadastro continua funcionando?* Abrir o Postman, clicar e ver "deu certo" é prova? E, quando o sistema crescer para três domínios, cinco desenvolvedores e uma janela de release, quanto tempo essa prova vai levar? A resposta a essas perguntas é a **pirâmide de testes** — e, no fim, ela é também a primeira decisão de **arquitetura** que vamos discutir.
>
> As três pausas continuam: 🎬 **Imagine o cenário**, 💭 **Pare e reflita** e ✍️ **Experimente agora**.

---

## 1. Qualidade não é ausência de bug — é o custo de mudar

A definição intuitiva de qualidade ("software de qualidade é software sem bug") é confortável e inútil, porque todo software tem bug. Uma definição operacional serve melhor:

> **Qualidade é o custo de mudar o sistema com segurança.**

Repare no que essa definição faz. Ela transforma qualidade de uma **propriedade moral** ("fizemos um bom trabalho") em uma **grandeza econômica** ("mudar esta regra custa 2 horas ou 2 semanas?"). E ela explica um fenômeno que você já viu: um sistema sem testes raramente cai. Ele apenas fica **caro** — cada alteração exige homologação manual completa, cada release vira evento, e a área acaba dizendo "não mexe nisso que funciona". A organização paga o preço em **lentidão**, não em incidentes visíveis. É a fatura mais silenciosa da TI.

Testar bem entrega três coisas concretas:

- **Redução de custo.** Testar custa esforço, sim. Mas o defeito que chega ao cliente custa muito mais — em correção emergencial, em retrabalho, em confiança.
- **Qualidade do produto.** Sistema testado tem menos retorno de defeito, e menos retorno significa mais capacidade de entregar coisas novas.
- **Segurança para evoluir.** Este é o principal para o nosso módulo. Nas Aulas 7 e 8 vamos **refatorar** este projeto — mudar sua estrutura interna sem mudar o comportamento. Isso é impossível sem testes: sem eles, refatorar não é refatorar, é reescrever e torcer.

> 💭 **Pare e reflita**
> Pense num trecho de código do seu trabalho em que ninguém quer mexer. Por que ninguém quer? Quase sempre a resposta não é "é difícil" — é "ninguém sabe o que vai quebrar".

---

## 2. Por que descobrir tarde custa caro

A ideia de que o custo de um defeito cresce conforme ele demora a ser encontrado vem de Barry Boehm, no início dos anos 1980. É intuitiva: um erro de regra pego pelo autor, no minuto seguinte, custa um minuto; o mesmo erro pego em produção custa investigação, correção emergencial, comunicação e, às vezes, reprocessamento de dados.

Uma nota de honestidade intelectual: os números específicos que circulam ("cada fase multiplica o custo por 10") são **contestados** na literatura de engenharia de software, e você faz bem em desconfiar de gráficos exponenciais sem fonte. O que é sólido é a **direção**: quanto mais tarde, mais caro. É nessa direção que a pirâmide se apoia — ela existe para empurrar a detecção para o **momento mais barato possível**, que é o segundo seguinte a você escrever a linha.

> 🎬 **Imagine o cenário**
> Duas equipes fazem a mesma mudança na regra de cotação. A primeira roda `mvn test`, vê um teste vermelho em 4 segundos e corrige. A segunda envia para homologação, aguarda dois dias, recebe um e-mail com print de tela, tenta reproduzir, descobre que o ambiente estava com dado antigo, corrige na terceira tentativa. **Mesmo bug, mesmo desenvolvedor.** O que mudou foi só o tempo até o feedback — e é isso que separa uma equipe rápida de uma equipe lenta.

---

## 3. A pirâmide de testes

A pirâmide foi proposta por **Mike Cohn** (*Succeeding with Agile*, 2009) e detalhada por **Ham Vocke e Martin Fowler** em *The Practical Test Pyramid*. Ela responde a uma pergunta prática: se posso escrever testes de vários tipos, **quantos de cada** devo escrever?

| Nível | O que verifica | Velocidade | Custo de escrever/manter | Confiança que dá | Quantidade |
|---|---|---|---|---|---|
| **Unitário** (base) | uma unidade isolada (uma regra, um método) | milissegundos | baixo | prova a **regra**, não o sistema | muitos |
| **Integração** (meio) | peças reais conversando: banco, JPA, HTTP | segundos | médio | prova a **fiação** | alguns |
| **Ponta a ponta / UI** (topo) | o fluxo inteiro, como o usuário faz | dezenas de segundos a minutos | alto (frágil) | prova que **funciona de verdade** | poucos |

A forma é uma pirâmide porque cada nível acima é mais lento, mais caro e mais frágil que o de baixo — mas dá um tipo de confiança que o de baixo não dá. Não se trata de "unitário é mais importante"; trata-se de **economia**: você compra o máximo de confiança pelo menor custo colocando o peso embaixo.

Duas deformações têm nome próprio, e você provavelmente já viu as duas:

- **Sorvete de casquinha invertido:** uma montanha de testes manuais e de interface no topo, quase nada embaixo. Cada release exige dias de homologação, e a suíte "automatizada" quebra sozinha toda semana.
- **Ampulheta:** muitos unitários e muitos e2e, nada no meio. Cada peça funciona, o conjunto não — porque ninguém testou a fiação.

<details><summary>Ver esquema em texto — a pirâmide e seus eixos</summary>

```
                    ▲  mais lento, mais caro,
                   ╱ ╲ mais frágil, mais realista
                  ╱E2E╲        poucos
                 ╱─────╲
                ╱ INTE- ╲      alguns
               ╱ GRAÇÃO  ╲
              ╱───────────╲
             ╱  UNITÁRIOS  ╲   muitos
            ╱───────────────╲
                    ▼  mais rápido, mais barato,
                       mais estável, mais isolado

  DEFORMAÇÕES COMUNS

   sorvete invertido        ampulheta
   ┌───────────────┐        ┌───────────────┐
   │   E2E manual  │        │      E2E      │
   ├───────────────┤        ├───┐       ┌───┤
   │  integração   │        │   │  nada │   │
   ├───┐       ┌───┤        ├───┘       └───┤
   │   │unitár.│   │        │   UNITÁRIOS   │
   └───┴───────┴───┘        └───────────────┘
   dias de homologação      peças ok, conjunto quebrado
```
</details>

---

## 4. Os três níveis, em código

Vamos escrever os três sobre o mesmo cadastro de cliente do Câmbio. Repare no que cada um **prova** — e no que cada um **não** prova.

### 4.1. Unitário: prova a regra

Sem Spring, sem banco, sem rede. As dependências viram **dublês** (mocks). Roda em milissegundos.

```java
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock ClienteRepository repositorio;
    @InjectMocks ClienteService servico;

    @Test
    void deveLancarExcecaoQuandoCpfNaoEstaCadastrado() {
        given(repositorio.findByCpf("43488428095")).willReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorCpf("43488428095"))
            .isInstanceOf(ClienteNaoEncontradoException.class);
    }
}
```

O nome do teste é parte do teste: `deveLancarExcecaoQuandoCpfNaoEstaCadastrado` descreve **comportamento**. `testBuscarPorCpf2` não descreve nada — e, quando falhar às 3 da manhã, não vai te ajudar.

### 4.2. Fatia web: prova o contrato HTTP

O unitário acima não sabe nada de HTTP. Ele não prova que o `POST` devolve **201**, que a validação devolve **400**, nem que a exceção vira **404**. Isso é responsabilidade da camada web — e `@WebMvcTest` sobe **só** essa fatia.

```java
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ClienteService servico;   // o serviço é dublê aqui

    @Test
    void deveDevolver400QuandoCpfAusente() throws Exception {
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "nome": "Maria", "dataNascimento": "1990-04-11" }
                    """))
            .andExpect(status().isBadRequest());
    }
}
```

Este teste prova que `jakarta.validation` e o `@ControllerAdvice` estão fazendo seu trabalho — algo que nenhum teste unitário do serviço poderia provar.

### 4.3. Integração: prova a fiação

Aqui nada é dublê: sobe a aplicação inteira, com H2 de verdade, e a requisição atravessa controller → serviço → JPA → banco.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CadastroClienteIT {

    @Autowired TestRestTemplate http;

    @Test
    void deveCadastrarEDepoisConsultarPorCpf() {
        var novo = new ClienteRequest("Maria", "43488428095",
                                      LocalDate.of(1990, 4, 11), "SOLTEIRO", "F");

        var criado = http.postForEntity("/clientes", novo, ClienteResponse.class);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var achado = http.getForEntity("/clientes/43488428095", ClienteResponse.class);
        assertThat(achado.getBody().cpf()).isEqualTo("43488428095");
    }
}
```

**Um** desses basta para o fluxo principal. Por que não vinte? Porque cada um custa segundos de subida de contexto, e vinte deles transformam `mvn test` num café — e uma suíte lenta é uma suíte que ninguém roda.

<details><summary>Ver esquema em texto — o que cada nível atravessa</summary>

```
                 HTTP   CONTROLLER   SERVICE   JPA/REPO   BANCO
 unitário         ─       ─          [REAL]    (mock)      ─
 @WebMvcTest    [REAL]  [REAL]       (mock)      ─         ─
 @SpringBootTest[REAL]  [REAL]       [REAL]    [REAL]    [REAL H2]
 cURL / .http   [REAL]  [REAL]       [REAL]    [REAL]    [REAL]  ← manual

 [REAL] = a peça de verdade    (mock) = dublê    ─ = nem entra no teste

 Quanto mais [REAL], mais confiança — e mais lento e mais frágil.
 A pirâmide é a escolha consciente de onde gastar cada tipo.
```

</details>

> ✍️ **Experimente agora**
> Escolha uma regra de negócio do seu trabalho e escreva **só o nome** de três testes para ela, no formato `deve<Comportamento>Quando<Condição>`. Se você não consegue nomear o comportamento, provavelmente a regra ainda não está clara — e esse é o verdadeiro achado.

---

## 5. cURL e cliente HTTP: onde isso cai na pirâmide?

Chamar a API rodando com `cURL` (ou com um arquivo `.http` no IDE, ou com o Postman) é indispensável:

```bash
curl -i -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria","cpf":"43488428095","dataNascimento":"1990-04-11","estadoCivil":"SOLTEIRO","sexo":"F"}'
```

Onde isso fica na pirâmide? É um **teste de API/serviço**: acima da integração, abaixo do e2e de interface — o sistema está de pé de verdade, mas você não passa pela tela do usuário.

E aqui está a distinção que separa a ferramenta do hábito: **executado à mão, isso não é um teste — é uma exploração.** Exploração é valiosíssima (você descobre o inesperado), mas ela não é **repetível**, não roda no build, não avisa ninguém quando quebra e não protege a refatoração de daqui a três semanas. Se você quer que a chamada HTTP vire rede de proteção, ela precisa virar código: é exatamente o que o `TestRestTemplate` do §4.3 faz.

> 💭 **Pare e reflita**
> Quantas vezes na semana você repete a mesma sequência de cliques para conferir que algo continua funcionando? Cada repetição é um teste automatizado que você ainda não escreveu.

---

## 6. Cobertura não é qualidade (e um gole de TDD)

**Cobertura** mede quais linhas foram **executadas** durante os testes. Ela não mede se alguém **verificou** o resultado. O teste abaixo dá 100% de cobertura no método e prova exatamente nada:

```java
@Test
void cadastrar() {
    servico.cadastrar(new Cliente("Maria", "43488428095")); // executa... e pronto.
}
```

Sem `assert`, o método rodou e o relatório ficou verde. Cobertura é um **detector de buracos** — útil para achar o que ninguém testou —, nunca uma meta. A meta melhor, e verificável: **todo comportamento de negócio tem um teste que falha se a regra for apagada.** Esse é o "teste do teste": apague a regra; se a suíte continuar verde, o teste era decoração.

E o **TDD** (escrever o teste antes do código)? Ele tem um módulo inteiro adiante nesta formação — aqui fica só a degustação. O ciclo é *vermelho → verde → refatorar*: escreva um teste que falha, faça-o passar da forma mais simples, então limpe o código com a rede de proteção já montada. O ganho menos óbvio do TDD não é o teste que sobra: é que **escrever o teste primeiro obriga você a projetar a classe do lado de fora**, pensando em como ela será usada. Classes difíceis de testar costumam ser classes mal desenhadas — o teste é o primeiro cliente do seu código.

---

## 7. O que é arquitetura de software

Mudamos de assunto — mas não tanto quanto parece. Testes são a rede que torna possível **mudar a estrutura** do sistema; arquitetura é justamente o estudo dessa estrutura.

A definição de arquitetura é notoriamente difusa. Vale ler algumas, porque cada uma ilumina um lado:

- *"Conjunto de elementos arquiteturais e sua organização, definidos por decisões tomadas para satisfazer objetivos e restrições."* (Perry e Wolf, 1992)
- *"A organização fundamental de um sistema, seus componentes, suas relações com o ambiente e os princípios que guiam seu design e evolução."* (ISO/IEEE 1471-2000)
- *"As decisões significativas de design que moldam um sistema, onde a significância é medida pelo **custo da mudança**."* (Grady Booch)
- *"O conjunto de decisões que você queria ter tomado no início do projeto, mas não teve imaginação para tanto."* (Ralph Johnson)

A de Booch é a mais útil no dia a dia, e casa com a §1 deste texto: **arquitetura é o subconjunto de decisões cuja mudança é cara.** Trocar o nome de uma variável é barato — não é arquitetura. Trocar o banco relacional por um NoSQL, ou quebrar um sistema em serviços, é caro — é arquitetura.

E note a diferença de escopo: **engenharia de software** cuida do processo, dos times e das métricas (foi o assunto da Aula 1); **arquitetura de software** cuida da estrutura da solução — quais componentes existem, como conversam, onde os dados moram.

---

## 8. Aspectos operacionais, estruturais e transversais

Decisões arquiteturais são cobradas por **características de qualidade**, tradicionalmente agrupadas em três famílias:

| Família | O que é | Exemplos | Na API de Câmbio |
|---|---|---|---|
| **Operacionais** | como o sistema se comporta rodando | disponibilidade, performance, tolerância a falhas, escalabilidade | se o provedor de cotação cair, a ordem falha? |
| **Estruturais** | como o código e a configuração se organizam | configuração, extensibilidade, portabilidade, manutenibilidade | trocar o provedor de cotação exige mudar o serviço? (Aula 4) |
| **Transversais** | atravessam todo o sistema e o negócio | acessibilidade, segurança, privacidade, viabilidade | CPF em log é vazamento de dado pessoal |

Duas ideias para levar daqui. Primeiro: essas características **competem entre si**. Mais disponibilidade custa mais infraestrutura; mais segurança custa latência; mais extensibilidade custa complexidade. Arquitetura é escolher **quais** características você vai privilegiar — e escrever isso, para que quem vier depois entenda a escolha. Segundo: repare que "viabilidade" está na lista. A solução tecnicamente mais elegante que a sua organização não consegue operar **não é a melhor solução**. Isso vai voltar, com força, na Aula 5.

> 🎬 **Imagine o cenário**
> Você propõe cache distribuído para acelerar a consulta de cotação. Elegante, moderno, resolve performance. Aí a área de infraestrutura informa que não há esse serviço homologado e que a aprovação leva um trimestre. A decisão arquiteturalmente correta pode ser um cache em memória, medíocre no papel e **entregue na sexta-feira**. Viabilidade é um requisito, não uma desculpa.

---

## 9. Por que a nossa API é um monólito

Olhe o que construímos: um único projeto Maven, um único artefato, um único processo, um único banco. Cliente mora ali; na Aula 3, Cotação e Ordem vão morar no mesmo lugar. Isso tem nome: **arquitetura monolítica** — todos os componentes de negócio concentrados em uma aplicação e um ambiente de execução.

E, para um sistema deste tamanho, isso é **acertado**, não uma dívida. As vantagens são reais: implantar é simples (um artefato), desenvolver é simples (um repositório, um `mvn test`), a comunicação entre partes é uma **chamada de método** (sem rede, sem serialização, sem falha parcial) e depurar é seguir um *stack trace* — e não correlacionar logs de cinco serviços.

As desvantagens também são reais, e aparecem com escala: você não consegue escalar **só** a parte que precisa (se a consulta de cotação recebe cem vezes mais carga, sobe tudo junto); uma falha em um ponto pode derrubar o conjunto; a base cresce e a velocidade do time cai; e qualquer correção obriga a reimplantar o todo.

A pergunta certa nunca é "monólito ou microsserviços?", e sim **"o que dói hoje e o que custa mudar?"**. Hoje, nada dói. A Aula 3 é sobre crescer esse monólito **com juízo** — para que, se um dia doer, a quebra seja possível.

---

## 10. Ponte com o legado

Se sua experiência inclui EJB em servidor de aplicação, EAR único e janelas de release, você conhece muito bem a face cara da falta de testes:

- **Homologação manual de dois dias** antes de cada versão, com planilha de casos e um testador clicando. Funciona — e não escala. A pirâmide propõe trocar a maior parte desse esforço por **segundos** de `mvn test`, reservando o humano para o que só o humano faz: exploração e julgamento.
- **"Só rodo no servidor"**: código legado que não roda fora do contêiner (EJB, JNDI, sessão, transação declarativa) é código **não testável em unidade** — e isso não é acidente. É consequência de dependências implícitas. O que torna o código de hoje testável é o mesmo que o tornou substituível: dependência **explícita**, injetada por construtor (voltaremos a isso na Aula 7).
- **Rotinas batch noturnas** que só falham em produção, às 3h. Um batch com a regra de negócio isolada numa classe pura é um batch com testes unitários. Um batch que lê arquivo, calcula e grava tudo no mesmo método só pode ser testado rodando de verdade.
- **Integrações SOAP** já tinham, no WSDL, o instinto certo: um **contrato** verificável entre sistemas. É o mesmo instinto do teste de contrato — e da OpenAPI da Aula 4.

---

## 11. IA & agentes hoje

Quando parte do código passa a ser gerada por um assistente, o teste muda de papel: deixa de ser boa prática e vira **o mecanismo de aceitação**.

- **O teste é o contrato.** Revisar 200 linhas geradas por leitura é o modo mais lento e menos confiável de revisar. Revisar **o teste** (que é curto e declara o comportamento esperado) e deixar a suíte julgar a implementação é mais rápido e mais seguro. Por isso o critério de aceite bem escrito da Aula 1 volta aqui: ele é a ponte entre o card e o teste.
- **IA gera ótimos casos de borda e péssimas prioridades.** Peça variações de CPF (dez dígitos, com letra, nulo, com máscara) e ela cobre tudo. Mas ela tende a escrever testes que descrevem **o que o código faz**, não o que o requisito **pede** — e um teste assim **congela o bug** em vez de encontrá-lo. Regra prática: o humano escreve o primeiro teste, aquele que define o comportamento correto; a IA multiplica as variações.
- **Quem testa quem?** Se o mesmo agente escreveu o código **e** os testes, os dois podem estar errados juntos, de forma coerente. A revisão humana do **caso de teste** é o ponto de controle que não dá para terceirizar.
- ***Evals* são a pirâmide aplicada a LLMs.** Avaliar um sistema de IA tem a mesma economia: na base, muitos casos determinísticos e baratos (formato da saída, presença de campos, regras duras); no meio, cenários de integração com ferramentas reais; no topo, poucas avaliações humanas, caras e insubstituíveis. Outro objeto, mesma pirâmide.

---

## 12. Para ir além

- **Martin Fowler e Ham Vocke, *The Practical Test Pyramid*** — [martinfowler.com/articles/practical-test-pyramid.html](https://martinfowler.com/articles/practical-test-pyramid.html). A referência do módulo, com exemplos em Java/Spring.
- **Martin Fowler, *TestPyramid*** (bliki) — a versão curta do conceito, útil para citar em reunião.
- **Mike Cohn, *Succeeding with Agile*** (2009) — onde a pirâmide foi proposta.
- **Documentação do Spring Boot — *Testing*** e do **JUnit 5** — `@WebMvcTest`, `@SpringBootTest`, `@MockitoBean` e o ciclo de vida dos testes, da fonte.
- **Mark Richards e Neal Ford, *Fundamentals of Software Architecture*** — a origem da classificação em características operacionais, estruturais e transversais da §8.

> **Na próxima aula (Aula 3 — arquitetura monolítica):** com a rede de proteção montada, o monólito vai **crescer**: entram os domínios **Cotação** e **Ordem** no mesmo projeto. E aí a pergunta deixa de ser "como testo?" e passa a ser "**onde ponho cada coisa?**" — camadas ou domínios, o que pode chamar o quê, e como registrar, num ADR de uma página, a decisão de continuar monolítico.
