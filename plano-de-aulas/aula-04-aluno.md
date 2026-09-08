# Material do Aluno — Aula 4: Arquitetura Orientada a Serviços × REST

> **Tempo de leitura:** ~16 min. Até agora a nossa API de Câmbio se bastava: o cliente está na nossa tabela, a ordem é nossa, e a cotação... a cotação **não é nossa**. Ela nasce lá fora, num provedor que muda de formato quando quer, fica lento quando o mercado abre e cai sem pedir licença. Nesta aula a gente para de tratar essa dependência como detalhe e passa a tratá-la como o que ela é: uma **fronteira**. E para falar de fronteira com propriedade, precisamos desfazer uma confusão de vocabulário que atravessa a indústria inteira — a diferença entre **ter APIs REST** e **ter arquitetura orientada a serviços**.

---

## 1. O problema: a cotação vem de fora

Hoje, no nosso monólito, o `OrdemService` faz três coisas em sequência: confirma o cliente, pega a cotação, calcula o total. As duas pontas são nossas. A do meio não.

🎬 **Imagine o cenário.** É terça-feira, 9h05. O provedor de cotação renomeia o campo `bid` para `bid_price` numa atualização "menor". Se o JSON dele estiver sendo desserializado direto na classe que o seu domínio usa, o efeito é imediato: `valorCotacao` chega `null`, o cálculo estoura, e a `POST /ordens` para de funcionar — no meio do horário de pico. O bug não está no seu código. Está na **ausência de fronteira** entre o código deles e o seu.

E há um segundo problema, bem concreto no nosso caso: **o provedor externo é inacessível** de onde o projeto roda. Não dá para "só chamar a API". Precisamos de um desenho em que a origem da cotação seja **substituível** — local quando não há saída para a internet, externa quando há — **sem que a regra de negócio saiba disso**.

💭 **Pare e reflita.** Quantos lugares do seu código atual quebrariam se o fornecedor de uma integração mudasse o nome de um campo amanhã? Se a resposta for "vários", esta aula é sobre isso.

---

## 2. O que é SOA, de verdade

**SOA** (*Service-Oriented Architecture*, ou arquitetura orientada a serviços) costuma ser apresentada como "arquitetura em que você tem serviços". Essa definição não ajuda ninguém: todo sistema tem alguma coisa chamada "serviço". O que caracteriza SOA é um conjunto de compromissos:

- **O serviço representa uma capacidade de negócio**, não uma tabela nem uma camada técnica. "Consultar Cotação" é serviço. "CRUD da tabela `cotacoes`" não é — é banco exposto.
- **O serviço tem contrato publicado.** Existe um documento (WSDL no mundo SOAP, OpenAPI no mundo HTTP) que descreve entradas, saídas e erros, e esse documento é o que o consumidor usa. O consumidor **não precisa ler o código** do provedor.
- **O serviço tem dono.** Um time responde por ele: por evoluir, por manter no ar, por avisar antes de quebrar.
- **O serviço é reutilizável por vários consumidores.** Este é o ponto que separa SOA de "API do meu app": o serviço de cotação atende ao app, ao internet banking, ao caixa da agência e ao relatório do fim do dia. Se só existe um consumidor possível, você tem uma camada, não um serviço.
- **Existe governança.** Catálogo de serviços, política de versionamento, controle de quem pode consumir, ciclo de vida (quando um serviço é aposentado). Sem isso, o reuso vira bagunça.
- **Serviços se compõem.** Um processo de negócio (por exemplo, "efetivar a compra de moeda") é uma **orquestração** de serviços menores.

Repare que **nada disso é sobre protocolo**. SOA nasceu no mundo SOAP/WSDL/XML porque era o que existia nos anos 2000, mas SOA não é SOAP. É um jeito de **repartir a responsabilidade dentro da organização** — e só depois um jeito de escrever código.

💭 **Pare e reflita.** Pense num "serviço" do seu trabalho. Ele atende a mais de um consumidor? Tem contrato escrito? Tem dono claro? Se as três respostas forem "não", ele é uma camada com nome bonito.

---

## 3. O ESB e por que ele virou palavrão

No auge do SOA corporativo, surgiu o **ESB** (*Enterprise Service Bus*, barramento de serviços). A ideia era razoável: em vez de N sistemas se conhecendo aos pares, todos falam com um barramento que faz **roteamento** (levar a mensagem ao serviço certo), **mediação** (traduzir formatos entre sistemas), **segurança** e **auditoria** num lugar só.

O problema não foi a ideia — foi o que aconteceu com ela. Um lugar central por onde tudo passa é um lugar **muito conveniente** para colocar "só mais uma regrinha". Em poucos anos, o barramento de muitas empresas acumulou transformação de payload, roteamento condicional e regra de negócio escrita em linguagem de transformação. O resultado tem nome:

- **Ponto único de falha** — o barramento cai, tudo cai.
- **Time-gargalo** — qualquer mudança em qualquer integração passa pelo time do barramento.
- **Deploy acoplado** — sistemas independentes que precisam subir juntos.

Ou seja: um **monólito distribuído**, com o pior dos dois mundos — a rigidez do monólito e a complexidade da rede. A lição que interessa a você não é "ESB é ruim". É: **mediação centralizada atrai lógica de negócio, e lógica de negócio centralizada é acoplamento**. Guarde isso; ela volta na Aula 6, quando falarmos de service discovery (outro componente central que precisa ser burro para ser seguro).

---

## 4. REST é um estilo, não uma arquitetura corporativa

REST (*Representational State Transfer*) foi descrito por Roy Fielding em 2000 como um **estilo arquitetural** para sistemas distribuídos na web. Em termos práticos, ele te dá:

- **Recursos identificados por URI.** `/cotacoes/USD` é uma coisa do mundo, não uma função. Nome de recurso é **substantivo**, não verbo: `/cotacoes/USD`, nunca `/buscarCotacao?moeda=USD`.
- **Verbos uniformes com semântica.** `GET` lê (e não muda nada), `POST` cria, `PUT` substitui (e é **idempotente**: repetir dá o mesmo resultado), `DELETE` remove.
- **Códigos de status como parte do contrato.** `201` para criado, `404` para não encontrado, `422` para semanticamente inválido, `503` para dependência indisponível. Devolver `200` com `{"erro": "..."}` no corpo é jogar fora metade do protocolo.
- **Stateless.** Cada requisição carrega o que precisa; o servidor não guarda sessão do cliente entre chamadas.
- **Representações.** O mesmo recurso pode ser devolvido em JSON, XML, etc.

E há o **HATEOAS** (*Hypermedia as the Engine of Application State*): a resposta traz **links** para as próximas transições possíveis, de modo que o cliente navegue pela API sem URLs cravadas no código. É o nível mais alto do modelo de maturidade de Richardson — e, honestamente, **quase ninguém implementa**. A maior parte do que o mercado chama de REST para no nível 2 (recursos + verbos + status). Vale saber que existe, vale saber que o que você faz não é REST "puro", e vale não perder o sono por isso.

<details><summary>Ver esquema em texto — modelo de maturidade de Richardson…</summary>

```
Nível 0  POST /api   {"acao":"buscarCotacao","moeda":"USD"}     "HTTP como envelope"
            └─ um endpoint só, tudo POST, o verbo está no corpo

Nível 1  POST /cotacoes  ·  POST /ordens                        "recursos"
            └─ já há substantivos, mas o verbo HTTP é sempre o mesmo

Nível 2  GET /cotacoes/USD → 200 · POST /ordens → 201           "verbos + status"
            └─ AQUI mora 95% do que se chama de REST (inclusive o nosso projeto)

Nível 3  GET /ordens/7 → 200 {..., "_links":{"comprovante":"/ordens/7/comprovante"}}
            └─ HATEOAS: a resposta ensina o cliente o que fazer em seguida
```
</details>

---

## 5. Ter API REST não é ter SOA

Aqui está o ponto central da aula, e é o argumento do artigo *"Estratégia de API não é estratégia de SOA"*, que está na bibliografia do módulo. As duas coisas respondem a perguntas **diferentes**:

| Pergunta | Responde |
|---|---|
| Como dois sistemas trocam mensagens? | **REST** (estilo de interface) |
| Quem é dono de qual capacidade de negócio, com que contrato e para quem? | **SOA** (arquitetura/organização) |

| Aspecto | "Temos APIs REST" | "Temos SOA" |
|---|---|---|
| Unidade | Endpoint | Serviço de negócio |
| Recorte | Costuma seguir a **tabela** ou a tela | Segue a **capacidade de negócio** |
| Consumidor | Em geral um (o app que pediu) | Vários, previstos desde o desenho |
| Contrato | Muitas vezes implícito ("veja o código") | Publicado, versionado, com política de mudança |
| Dono | Difuso | Um time responde pelo serviço |
| Governança | Inexistente ou só o gateway | Catálogo, ciclo de vida, políticas |

Uma empresa pode publicar 200 endpoints REST num gateway e **não ter nenhuma** arquitetura orientada a serviços — se cada endpoint for o espelho de uma tabela, criado sob demanda para uma tela específica, sem dono e sem contrato. Isso é um banco de dados com HTTP na frente. E o inverso também vale: dá para ter SOA de verdade com SOAP, com gRPC, com fila de mensagens.

🎬 **Imagine o cenário.** O time A precisa do saldo. Cria `GET /saldoParaTelaHome`. O time B precisa do mesmo saldo, com um campo a mais. Cria `GET /saldoParaExtrato`. Seis meses depois há onze endpoints de saldo, cada um com uma regra ligeiramente diferente de "o que conta como saldo". Ninguém sabe qual é o certo. **Isso é o oposto de reuso** — e a diferença entre um serviço e um endpoint.

💭 **Pare e reflita.** No sistema em que você trabalha, quem responderia à pergunta *"qual é a definição oficial de cliente ativo?"* — uma pessoa, um documento, ou "depende de qual API você chamar"?

---

## 6. Contrato explícito: OpenAPI e versionamento

Se o serviço tem um contrato, o contrato precisa existir **fora da cabeça de quem escreveu**. No mundo HTTP, o formato padrão é o **OpenAPI**: um documento (YAML ou JSON) que descreve caminhos, parâmetros, corpos, tipos e respostas de erro.

Com Spring Boot, o springdoc gera esse documento a partir do próprio código e serve uma interface navegável:

```java
@GetMapping("/cotacoes/{moeda}")
@Operation(summary = "Consulta a cotação vigente de uma moeda suportada")
@ApiResponse(responseCode = "200", description = "Cotação encontrada")
@ApiResponse(responseCode = "422", description = "Moeda não suportada")
public CotacaoResponse consultar(@PathVariable Moeda moeda) { ... }
```

> **Importante:** o contrato **não depende da ferramenta**. Se o springdoc não estiver disponível no seu ambiente, um `docs/contrato-cotacao.md` escrito à mão — endpoints, campos, tipos, erros, exemplos — cumpre o papel. O que não pode existir é "o contrato é ler o controller".

**Versionamento.** Contrato publicado é promessa. Toda mudança cai em uma de duas categorias:

- **Compatível** (não quebra ninguém): acrescentar um campo **opcional** na resposta, acrescentar um novo endpoint, aceitar um parâmetro opcional novo.
- **Incompatível** (quebra): remover ou renomear campo, mudar tipo, tornar obrigatório o que era opcional, mudar o significado de um código de status.

Para mudança incompatível, você **versiona**: `/v1/cotacoes` e `/v2/cotacoes` convivendo por um período combinado, com data de desligamento anunciada. A regra prática é o **princípio da robustez**: seja rigoroso no que você envia e tolerante no que aceita — em particular, **ignore campos desconhecidos** na resposta de terceiros, em vez de estourar.

✍️ **Experimente agora.** Abra a resposta de `POST /ordens` do nosso projeto e classifique cada mudança: (a) trocar `valor_total_operacao` de número para string; (b) acrescentar `taxa_servico`; (c) renomear `cpf_cliente` para `documento_cliente`. Quais quebram um consumidor existente?

---

## 7. Adapter e Anti-Corruption Layer: a fronteira em código

Voltemos à cotação. A resposta do provedor externo é mais ou menos assim:

```json
{ "USDBRL": { "code": "USD", "bid": "5.4312", "ask": "5.4318",
              "create_date": "2026-09-16 09:05:11" } }
```

Três coisas erradas para o nosso domínio: a chave é `USDBRL` (concatenação de moedas), os números vêm como **texto**, e a data tem formato próprio. Se essa estrutura entrar no domínio, o domínio herda os problemas do fornecedor.

A solução tem dois nomes que você vai ouvir sempre:

- **Adapter** (padrão estrutural do catálogo GoF, que a Aula 8 vai formalizar): uma classe cuja única função é **traduzir** a forma externa para a forma interna.
- **Anti-Corruption Layer (ACL)**: o mesmo mecanismo visto pela ótica estratégica — uma camada deliberada que impede que o modelo de outro sistema "contamine" o seu. O nome é forte de propósito: o modelo alheio é tratado como algo que **corrompe** o seu se atravessar a fronteira.

Na prática: você define **a sua** interface, e a implementação externa é quem se vira para atendê-la.

```java
public interface CotacaoProvider {              // o CONTRATO é nosso
    Cotacao buscar(Moeda moeda);                // tipos nossos, dos dois lados
}

@Component
@ConditionalOnProperty(name = "cotacao.provedor", havingValue = "externo")
class CotacaoExternaProvider implements CotacaoProvider {

    private final RestClient http = RestClient.create("https://economia.awesomeapi.com.br");

    public Cotacao buscar(Moeda moeda) {
        RespostaAwesome externa = http.get()
                .uri("/{moeda}/", moeda.name())
                .retrieve()
                .body(RespostaAwesome.class);   // tipo EXTERNO, vive só aqui dentro
        return adaptar(externa, moeda);         // sai do método como tipo NOSSO
    }
}
```

A regra de ouro cabe numa frase: **nenhum tipo do fornecedor atravessa a interface**. `RespostaAwesome` nasce e morre dentro do adapter. Se o fornecedor renomear `bid`, muda **uma linha, em um arquivo** — e nem o `OrdemService` nem os testes ficam sabendo.

<details><summary>Ver esquema em texto — a fronteira do provedor de cotação…</summary>

```
        DOMÍNIO (nosso modelo, nossas regras)
   ┌──────────────────────────────────────────────┐
   │  OrdemService                                │
   │      └─ depende de ─► CotacaoProvider  (interface: Cotacao buscar(Moeda))
   └──────────────────────────┬───────────────────┘
                              │   ← FRONTEIRA (nada externo passa daqui)
        ┌─────────────────────┴─────────────────────┐
        ▼                                           ▼
  CotacaoLocalProvider                      CotacaoExternaProvider
  (lê a tabela `cotacoes` no H2)            (RestClient → awesomeapi)
  cotacao.provedor=local  [padrão]          cotacao.provedor=externo
                                                    │
                                                    ▼
                                            adaptar(RespostaAwesome → Cotacao)
                                            "USDBRL", "5.4312", "2026-09-16 09:05:11"
```
</details>

---

## 8. No projeto: um contrato, dois provedores

O incremento desta aula no Câmbio é pequeno em linhas e grande em consequência:

1. `CotacaoProvider` passa a ser **interface** — o contrato do serviço de cotação.
2. `CotacaoLocalProvider` lê a tabela `cotacoes` do H2 (semeada com USD ≈ 5,43 e EUR ≈ 6,58). É o **padrão**.
3. `CotacaoExternaProvider` chama o provedor real com `RestClient` e **adapta** a resposta. Só é ativado com `cotacao.provedor=externo` e só funciona onde há saída para a internet.
4. `PUT /cotacoes/{moeda}` atualiza a cotação local — assim dá para **simular variação de mercado** e ver o `valor_total_operacao` mudar entre duas ordens.
5. O contrato é documentado (OpenAPI ou arquivo em `docs/`).

```yaml
# application.yml
cotacao:
  provedor: local     # troque para "externo" fora da rede corporativa
```

O ganho que vale a aula inteira: **`OrdemService` não muda uma única linha** quando o provedor troca. Você acabou de aplicar, sem saber ainda o nome, dois princípios que a Aula 7 vai formalizar — **aberto/fechado** (novo provedor sem alterar o existente) e **inversão de dependência** (a regra depende da abstração, não do detalhe).

✍️ **Experimente agora.** Escreva um `CotacaoFixaProvider` que devolve sempre `4.00` e use-o num teste de `OrdemService`. Note que o teste roda **sem banco e sem rede**, em milissegundos. Contrato bom é contrato que também serve de costura de teste.

💭 **Pare e reflita.** Se o provedor externo ficar fora do ar, o que a sua API deve responder: `503`, ou a última cotação conhecida? Não há resposta única — mas em domínio financeiro, **servir preço velho sem avisar** costuma ser pior que assumir a indisponibilidade. Essa é uma decisão de negócio, e por isso ela merece um ADR.

---

## 9. Ponte com o legado

Se você vem de um ambiente corporativo tradicional, quase tudo desta aula tem um parente que você já conhece:

- **Web service SOAP com WSDL** é contrato publicado — e, em alguns aspectos, era **mais rigoroso** que o OpenAPI médio de hoje: tipos fortes, validação por schema, geração automática de cliente. O que ficou pesado foi o envelope XML, o ecossistema WS-* e a cerimônia.
- **EJB remoto** é chamada de serviço com interface explícita — a interface remota era, na prática, o contrato.
- **EAR único com vários módulos** é o monólito da Aula 3: componentes separados no código, indivisíveis no deploy.
- **Barramento corporativo** é o ESB da §3, com as consequências da §3.

Traduzir isso importa por dois motivos. Primeiro, porque quem já operou um barramento **já viu** os problemas que os microsserviços das próximas aulas tentam resolver — e também os que eles **não** resolvem. Segundo, porque a modernização real de um parque legado raramente é "reescrever tudo": é **colocar contratos explícitos nas bordas do que existe** e ir substituindo o que está atrás deles. O adapter que você escreveu hoje é exatamente a ferramenta dessa transição — e, na Aula 5, ele vira a peça que permite arrancar um pedaço do monólito sem que o resto perceba.

---

## 10. IA & agentes hoje

O que você fez nesta aula tem uma tradução quase literal no mundo de agentes:

- **Contrato de serviço = *tool definition*.** Quando você entrega uma ferramenta a um agente, escreve exatamente o que escreveu na interface: nome, parâmetros, tipos, o que a ferramenta faz e o que ela devolve quando falha. `CotacaoProvider` é, na forma, uma definição de *tool*. A diferença é o consumidor: em vez de outro programador ler seu contrato, é um modelo lendo.
- **OpenAPI virou língua franca humano↔agente.** O mesmo documento serve para o dev, para o gerador de cliente e para o agente decidir se aquela operação é a certa. Consequência prática desconfortável: uma descrição vaga de campo (`"tipo: string"`, sem dizer o quê) antes era desleixo de documentação; agora **degrada o comportamento** de quem consome. Documentação virou interface de execução.
- **ACL para APIs de modelo.** Fornecedores de LLM mudam nomes de modelo, formatos de resposta e limites com frequência **maior** que fornecedores tradicionais. Se a chamada ao modelo estiver espalhada em vinte lugares, cada troca é uma refatoração. Uma interface própria (`GeradorDeResumo`, e não `ClienteDoFornecedorX`) com adapter na borda é a mesma técnica de hoje, aplicada à dependência mais volátil do momento.
- **Governança volta ao centro.** SOA morreu de excesso de burocracia e está renascendo pela porta dos fundos: quando dezenas de ferramentas ficam disponíveis a agentes, alguém precisa responder "quais existem, quem é dono, quem pode chamar, o que acontece se mudar". É catálogo de serviços com outro nome.

---

## 11. Para ir além

- **Vertigo — *"Estratégia de API não é estratégia de SOA"*** (`vertigo.com.br/estrategia-de-api-nao-e-estrategia-de-soa/`): a referência do módulo para esta aula; leia como argumento, não como resumo.
- **Roy Fielding**, *Architectural Styles and the Design of Network-based Software Architectures* (2000), capítulo 5: a fonte original do REST.
- **Martin Fowler**, *Richardson Maturity Model* (martinfowler.com): os quatro níveis da §4, explicados com exemplos.
- **Eric Evans**, *Domain-Driven Design*: o capítulo de *context map* é onde a **Anti-Corruption Layer** é definida.
- **OpenAPI Specification** (`spec.openapis.org`) e a documentação do **springdoc-openapi**: o formato e a ferramenta.
- **The Open Group**, *SOA Source Book* (também na bibliografia do módulo): SOA descrita por quem a padronizou, sem hype.

> **Na próxima aula (Aula 5 — quebrando o monólito):** o nosso monólito agora tem contratos internos bem desenhados, e isso levanta a pergunta honesta — se está tão bem organizado, **por que separar?** Vamos olhar o que microsserviços realmente compram (deploy independente, escala seletiva, autonomia de time) e o que eles cobram (custo operacional, consistência, latência, depuração), decidir **por onde cortar** o Câmbio e, principalmente, discutir **quando não quebrar**. No fim da aula teremos três serviços rodando em três portas — e um problema novo que a Aula 6 vai resolver.
