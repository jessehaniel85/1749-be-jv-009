# Material do Aluno — Aula 6: Comunicação entre microsserviços (discovery e cliente declarativo)

> **Tempo de leitura:** ~17 min. Na aula passada quebramos o monólito e ganhamos três serviços — e uma linha desconfortável no `application.yml`: `servicos.cliente.url: http://localhost:8081`. Ela funciona no seu notebook. Não funciona quando existem três instâncias do `cliente-service`, em máquinas com IP dinâmico, que viram seis no horário de pico e voltam a três à noite. Nesta aula, o endereço deixa de ser configuração e vira **pergunta feita em tempo de execução**: *quem, agora, atende por `cliente-service`?* Vamos montar quem responde essa pergunta (o **service discovery**), trocar o cliente HTTP escrito à mão por um **cliente declarativo**, colocar timeouts que valem alguma coisa — e terminar com a pergunta que todo arquiteto precisa ter feito uma vez: **e quando quem responde onde as coisas estão é justamente quem cai?**

---

## 1. O problema: a URL fixa

🎬 **Imagine o cenário.** 20h de uma quarta-feira. O `cliente-service` está sofrendo com carga e o time sobe mais duas instâncias — endereços novos, atribuídos pelo orquestrador. Perfeito, exceto por um detalhe: o `cambio-service` continua chamando **um** endereço, cravado no arquivo de configuração. As duas instâncias novas ficam ociosas enquanto a antiga apanha. Para aproveitá-las, alguém precisa editar um arquivo, reconstruir o artefato e **reimplantar** o `cambio-service` — no meio do pico.

E há o caso pior: a instância cravada **morre**. Agora o `cambio-service` chama um endereço que não existe mais e falha 100% das vezes, mesmo com duas instâncias saudáveis do vizinho no ar.

O problema, dito com precisão: **o endereço de um serviço é um dado que muda com a mesma frequência que a infraestrutura**, e nós o guardamos num lugar que só muda com deploy. Está no lugar errado.

💭 **Pare e reflita.** No seu ambiente, quanto tempo leva entre "subiu uma instância nova" e "ela começou a receber tráfego"? Se a resposta envolve alguém editando arquivo, você tem esse problema.

---

## 2. Service discovery: registrar, renovar, resolver

A solução é um componente que **sabe quem está no ar**. Ele tem três verbos, e entender os três é entender tudo o que vem depois:

1. **Registrar.** Ao subir, cada instância se anuncia: *"eu sou uma instância de `cliente-service`, estou em tal endereço e porta"*. Ninguém precisa saber dela de antemão.
2. **Renovar (*heartbeat*).** Periodicamente, a instância manda um sinal de vida. Se parar de mandar por algum tempo, o registro a considera morta e a remove. Repare: o registro **não sabe** que ela morreu — ele **deduz**, por ausência de sinal. Essa distinção explica por que a remoção nunca é instantânea.
3. **Resolver.** Quem quer chamar pergunta pelo **nome lógico** (`cliente-service`) e recebe a **lista** das instâncias vivas.

O armazenamento desse mapa "nome → instâncias" tem um nome próprio: **service registry**. É a peça mais crítica do arranjo — se ela some, ninguém acha ninguém.

<details><summary>Ver esquema em texto — os três verbos…</summary>

```
                    ┌──────────────────────────────┐
                    │      SERVICE REGISTRY        │
                    │  cliente-service             │
                    │    ├─ 10.0.0.7:8081  ♥ 12s   │
                    │    └─ 10.0.0.9:8081  ♥  4s   │
                    │  cotacao-service             │
                    │    └─ 10.0.0.4:8082  ♥  8s   │
                    └──────────────────────────────┘
                       ▲            ▲            │
             (1) registrar     (2) heartbeat     │ (3) resolver
             (3) heartbeat      a cada N s       │ "quem é cliente-service?"
                       │            │            ▼
              ┌────────┴────────────┴───┐   ┌──────────────────┐
              │  instâncias de           │   │  cambio-service  │
              │  cliente-service         │◄──┤  escolhe uma e   │
              │  cotacao-service         │   │  chama           │
              └──────────────────────────┘   └──────────────────┘
```
</details>

O ganho conceitual é uma troca: você para de acoplar seu código a **onde** o vizinho está e passa a acoplá-lo a **quem** ele é. Endereço é volátil; nome lógico é estável.

---

## 3. Client-side × server-side discovery

Há duas formas de organizar isso, e você vai encontrar as duas na vida real.

| | **Client-side** (Eureka, Consul + biblioteca) | **Server-side** (balanceador, ingress, DNS interno) |
|---|---|---|
| Quem escolhe a instância | O **chamador**, com a lista em mãos | A **infraestrutura**, na frente do serviço |
| O que o chamador conhece | O nome lógico + o registro | Um endereço estável só |
| Saltos de rede | 1 (direto na instância) | 2 (chamador → balanceador → instância) |
| Onde vive a lógica | Numa biblioteca dentro da sua aplicação | Fora da aplicação, na plataforma |
| Acoplamento | A aplicação depende do cliente de discovery | Aplicação agnóstica |
| Típico de | Ecossistema Spring Cloud / Netflix OSS | Kubernetes, service mesh, balanceadores tradicionais |

💭 **Pare e reflita.** Olhe a linha "onde vive a lógica". No client-side, a política de escolha de instância é **código na sua aplicação** — se você tem serviços em quatro linguagens, mantém quatro implementações. Esse é o argumento que mais empurra times para o server-side.

Hoje, o mercado pende fortemente para o **server-side**: em Kubernetes, um `Service` te dá nome estável e balanceamento sem que a aplicação saiba de nada. Então por que estudamos o client-side com Eureka? Por dois motivos honestos: (a) é o que o módulo pede e o que roda sem infraestrutura na sua máquina; e (b) o Eureka te deixa **ver o mecanismo** — o registro, os heartbeats, a instância sumindo da lista com atraso. Esse comportamento é **idêntico** no Kubernetes, só que escondido. Quem entendeu o mecanismo aqui não se surpreende lá.

---

## 4. Eureka no projeto

O Eureka tem duas metades: o **servidor** (o registry) e o **cliente** (a biblioteca que registra e resolve).

```java
@SpringBootApplication
@EnableEurekaServer          // discovery-server, porta 8761
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
```

```yaml
# cliente-service/application.yml — idem para cotacao-service e cambio-service
spring:
  application:
    name: cliente-service        # ← o NOME LÓGICO; substitui a URL
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

Nas versões atuais do Spring Cloud, ter o starter de cliente no classpath já basta — `@EnableDiscoveryClient` é opcional e aparece muito em material mais antigo. Você pode encontrá-la assim:

```java
@SpringBootApplication
@EnableDiscoveryClient       // opcional nas versões atuais; explícito não faz mal
public class ClienteServiceApplication { }
```

✍️ **Experimente agora.** Suba o `discovery-server` e abra `http://localhost:8761`. Veja a tabela vazia. Suba o `cliente-service`, espere alguns segundos e recarregue: ele aparece em *Instances currently registered with Eureka*. Suba uma segunda instância (`--server.port=8091`) e veja **duas linhas sob o mesmo nome**. É esse mapa que o `cambio-service` vai consultar.

---

## 5. Cliente declarativo: OpenFeign

Na Aula 5 escrevemos o chamador à mão: montar a URL, escolher o método, desserializar. Trabalho repetitivo, e cada chamada nova repete o mesmo ritual.

A alternativa é **declarativa**: você escreve uma **interface** que descreve a chamada, e a biblioteca gera a implementação.

```java
@FeignClient(name = "cliente-service")   // NOME LÓGICO, não URL
public interface ClienteApi {

    @GetMapping("/clientes/{cpf}")
    ClienteResponse porCpf(@PathVariable String cpf);
}
```

```java
@FeignClient(name = "cotacao-service")
public interface CotacaoApi {

    @GetMapping("/cotacoes/{moeda}")
    CotacaoResponse porMoeda(@PathVariable Moeda moeda);
}
```

E no serviço, é só injetar — como qualquer bean:

```java
@Service
public class OrdemService {

    private final ClienteApi clientes;
    private final CotacaoApi cotacoes;

    public OrdemService(ClienteApi clientes, CotacaoApi cotacoes) {
        this.clientes  = clientes;
        this.cotacoes  = cotacoes;
    }
    // ...
}
```

Repare no que aconteceu com o `application.yml`: **`servicos.cliente.url` sumiu**. O `cambio-service` não conhece mais nenhum endereço — só nomes. Se amanhã o `cliente-service` mudar de máquina, de porta ou de quantidade de instâncias, nada aqui muda.

O ganho de fundo não é digitar menos: é que **o contrato do vizinho virou uma interface Java**. Ela pode ser revisada em code review, mockada em teste e injetada como abstração — e é por isso que a Aula 7 vai voltar aqui para falar de **inversão de dependência**.

💭 **Pare e reflita.** A interface `ClienteApi` descreve exatamente o mesmo contrato que o `cliente-service` publica no seu OpenAPI. Quem garante que as duas não vão divergir com o tempo? (Não há mágica: ou é geração a partir do contrato, ou é teste de contrato, ou é disciplina — e disciplina falha.)

---

## 6. Balanceamento client-side

Com o Feign integrado ao discovery, uma coisa acontece de graça: se `cliente-service` tiver três instâncias registradas, as chamadas se distribuem entre elas — por padrão em *round-robin*. Não há balanceador na frente; **o próprio chamador escolhe**.

<details><summary>Ver esquema em texto — quem escolhe a instância…</summary>

```
CLIENT-SIDE (o nosso)                    SERVER-SIDE (k8s / LB)

cambio-service                           cambio-service
   │ 1. "quem é cliente-service?"           │
   ▼                                        │ chama http://cliente-service
[registry] → [i1, i2, i3]                   ▼
   │                                     ┌────────────┐
   │ 2. escolhe i2 (round-robin)         │ balanceador│ escolhe i2
   ▼                                     └─────┬──────┘
  i2  (1 salto de rede)                        ▼
                                              i2   (2 saltos de rede)
```
</details>

Vantagem: um salto de rede a menos e controle fino da política de escolha. Desvantagem: a política vive **dentro** de cada aplicação — se você tem serviços em quatro linguagens, tem quatro implementações para manter coerentes. É exatamente esse argumento que empurrou o mercado para o server-side e para *service meshes*.

---

## 7. Timeouts, falha parcial e o mínimo de resiliência

Aqui está a parte que separa "funcionou na demo" de "aguenta produção".

**Timeout não é detalhe de configuração — é decisão de disponibilidade.** Sem timeout explícito, o padrão da biblioteca vira o seu SLA, e esse padrão costuma ser generoso demais. Com um vizinho lento, as suas threads ficam presas esperando; quando acabam, **você também está fora** — e sua indisponibilidade não tem nada a ver com o seu código.

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: 2000      # 2 s para estabelecer conexão
            readTimeout: 2000         # 2 s para a resposta chegar
```

**Falha parcial** é o estado novo que a Aula 5 criou: o sistema não está mais "no ar" ou "fora" — ele pode estar **meio no ar**. Para cada dependência, decida antes o que acontece:

| Dependência fora | Resposta possível | Quando faz sentido |
|---|---|---|
| `cliente-service` | `503`, rápido e honesto | O cliente **precisa** ser validado; não há ordem sem ele |
| `cotacao-service` | `503`, ou (decisão de negócio) usar cotação local recente | Em domínio financeiro, servir preço velho **sem avisar** costuma ser pior que falhar |
| Serviço de notificação | Seguir em frente e enfileirar | O usuário não está esperando por isso |

E há uma armadilha específica do discovery que você vai ver ao vivo: **remover uma instância morta leva tempo**. O registro deduz a morte pela ausência de heartbeat (dezenas de segundos, por padrão) e o chamador ainda mantém um **cache** da lista. Resultado: por algum tempo depois de a instância morrer, ela continua sendo escolhida — e algumas chamadas falham mesmo havendo instâncias saudáveis.

É daí que nascem os dois padrões que você vai ouvir sempre:

- **Retry** — tentar de novo, preferencialmente **em outra instância**. Resolve a falha transitória. Perigoso quando aplicado cegamente: se o serviço está caindo por sobrecarga, repetir a chamada **aumenta** a carga e piora a situação. Só faz sentido em operação idempotente.
- **Circuit breaker** — depois de N falhas seguidas, o chamador **para de tentar** por um tempo e falha imediatamente, dando ao vizinho a chance de se recuperar (e devolvendo threads a você). Depois, testa o terreno com uma chamada.

> Aqui ficamos no nome e no propósito. Implementação, políticas de retry, *bulkhead* e *fallback* são assunto do módulo seguinte da trilha. O que importa levar hoje é o **porquê**: eles existem porque descoberta e propagação **não são instantâneas**.

---

## 8. O registro também é um ponto de falha

Toda solução de arquitetura cria um problema novo, e é intelectualmente honesto olhar para ele. Ao remover o acoplamento a endereços, criamos uma **dependência crítica**: se ninguém responde "onde está o `cliente-service`?", ninguém acha ninguém.

🎬 **Imagine o cenário.** Em outubro de 2021, uma das maiores plataformas do mundo ficou horas fora do ar. Pelo relato público de engenharia da própria empresa, uma mudança de configuração em manutenção de rotina fez os servidores de nomes pararem de anunciar seus endereços — e, com isso, os nomes deixaram de resolver. Os serviços estavam lá; **o mecanismo que dizia onde eles estavam** é que sumiu. O agravante que mais ensina: as ferramentas internas de operação dependiam da mesma infraestrutura, o que dificultou até o diagnóstico. Padrão parecido apareceu em outubro de 2025 num incidente amplamente reportado de um grande provedor de nuvem, atribuído no relato público a uma condição de corrida na automação de DNS de um endpoint em uma região — com efeito em cascata para inúmeros serviços dependentes.

Três lições práticas:

- **Redundância no registro.** Registry é infraestrutura crítica: mais de uma instância, replicação entre elas. Uma instância única de Eureka num servidor é um ponto único de falha com nome bonito.
- **Cache local no cliente e degradação.** Um cliente de discovery bem feito **guarda a última lista conhecida** e continua operando se o registry sumir. Você perde a atualização (instância nova não entra, morta não sai), mas não perde tudo. Pergunta de projeto: *o seu sistema prefere parar ou operar com informação possivelmente velha?*
- **Não faça o diagnóstico depender do que quebrou.** Se o painel, o log e o acesso de emergência passam pela mesma peça que caiu, você fica cego exatamente quando mais precisa enxergar.

A mesma lógica vale para **configuração centralizada** (Spring Cloud Config, Consul, ConfigMap): tirar parâmetros do artefato é ótimo, e cria a mesma dependência crítica, com as mesmas respostas — redundância, cache local, degradação prevista.

### Plano C: o mesmo desenho sem Spring Cloud

Se o Eureka não estiver disponível no seu ambiente, **o padrão não depende dele**. Dá para ter cliente declarativo com Spring puro, usando `@HttpExchange`:

```java
@HttpExchange("/clientes")
public interface ClienteApi {
    @GetExchange("/{cpf}")
    ClienteResponse porCpf(@PathVariable String cpf);
}
```

```java
@Bean
ClienteApi clienteApi(@Value("${servicos.cliente.url}") String baseUrl) {
    RestClient http = RestClient.create(baseUrl);
    return HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(http))
            .build()
            .createClient(ClienteApi.class);
}
```

E dá para simular o registro com uma **lista de instâncias** por nome lógico no `application.yml`, escolhida em *round-robin* por um resolvedor de quinze linhas:

```yaml
servicos:
  cliente-service:
    instancias: [ "http://localhost:8081", "http://localhost:8091" ]
```

Compare com o Eureka: nome lógico → lista de instâncias → escolha. **É o mesmo desenho.** A única diferença é quem mantém a lista: aqui, uma pessoa editando um arquivo; lá, as próprias instâncias, por heartbeat. Essa diferença é justamente o valor do discovery — e enxergá-la assim, lado a lado, costuma ensinar mais do que só ligar o Eureka e ver funcionar.

---

## 9. Ponte com o legado

Descoberta de serviço não é invenção da era da nuvem — você provavelmente já usou, com outro nome:

- **JNDI** é service discovery. Você não abria um socket para o endereço de um EJB remoto: fazia *lookup* por **nome** num diretório, e o servidor devolvia a referência. Registro, nome lógico, resolução em tempo de execução — os três verbos do §2, em 1999.
- **O `jndi.properties` e o arquivo de propriedades por ambiente** são o "registro estático" do Plano C: alguém mantém a lista à mão. Funciona bem enquanto os endereços mudam raramente — que era a realidade de um datacenter com máquinas fixas, e deixou de ser a realidade de ambientes elásticos.
- **O balanceador corporativo na frente do cluster** é discovery **server-side**, e talvez já seja a resposta certa para boa parte do seu parque. Não há prêmio por adotar registro dinâmico onde os endereços não mudam.
- **O barramento corporativo** também fazia localização, junto com mediação e transformação — e é aí que mora a diferença que importa: um registry **só responde onde as coisas estão**. Ele é deliberadamente burro. Quando o componente central começa a fazer também tradução e regra de negócio, você reinventou o ESB da Aula 4 e ganhou o gargalo de volta.

---

## 10. IA & agentes hoje

- **Registro de ferramentas é service discovery.** Quando um agente descobre em tempo de execução quais ferramentas existem — via **MCP** (Model Context Protocol) ou protocolos de agente para agente —, ele está fazendo *lookup* por nome e capacidade, exatamente como o `cambio-service` pergunta por `cliente-service`. Registro, catálogo, resolução: mesmos três verbos.
- **Cliente declarativo é *tool use* declarativo.** Você descreve nome, parâmetros e tipos; a plataforma executa a chamada. O Feign é a versão determinística disso; o agente é a versão probabilística — quem **escolhe** a ferramenta é o modelo, não o seu `if`.
- **O ponto de falha fica mais grave.** Se o registry cai, o `cambio-service` falha — de forma feia, mas previsível. Se o catálogo de ferramentas some, o agente **muda de comportamento**: tenta resolver sem a ferramenta e pode inventar uma resposta. Descoberta indisponível deixa de ser problema de disponibilidade e vira problema de **corretude**. Qual é o comportamento correto de um agente que não achou a ferramenta? Falhar explicitamente costuma ser a resposta certa — e precisa ser decidido por você, não pelo modelo.
- **Timeout também vale para modelo.** Uma chamada de LLM é lenta e de duração muito variável. Sem limite de tempo e sem plano para o caso de estouro, um passo de agente prende a sua requisição do mesmo jeito que um vizinho lento prende as suas threads.

---

## 11. Para ir além

- **Chris Richardson**, `microservices.io` — padrões *Service Registry*, *Client-side Discovery*, *Server-side Discovery*: os três desenhos deste material, na fonte.
- **Documentação do Spring Cloud** — *Spring Cloud Netflix (Eureka)* e *Spring Cloud OpenFeign*: configuração, timeouts e integração com discovery.
- **Spring Framework — HTTP Interface (`@HttpExchange`)**: o cliente declarativo sem Spring Cloud, usado no Plano C.
- **Sam Newman**, *Building Microservices* (2ª ed.), capítulos de comunicação e de resiliência: por que timeout, retry e circuit breaker vêm sempre juntos.
- **Michael Nygard**, *Release It!*: a origem prática do circuit breaker e o melhor catálogo de modos de falha em produção.
- **Relatórios públicos de incidentes** — o relato de engenharia da Meta sobre 04/10/2021 e os *post-mortems* públicos de provedores de nuvem: ler incidente alheio é a forma mais barata de aprender arquitetura.

> **Na próxima aula (Aula 7 — Clean Code + SOLID):** o sistema funciona. Agora abra o `OrdemService` e conte quantas coisas ele faz: valida cliente, busca cotação, calcula valor, monta resposta, grava. Conte também quantos números mágicos sobraram (4 dígitos de agência, 11 de CPF, escala 2) e quantos `@Autowired` em campo ficaram pelo caminho. Nas próximas duas aulas vamos **refatorar sem mudar comportamento** — e os testes da Aula 2 são exatamente a rede que torna isso seguro.
