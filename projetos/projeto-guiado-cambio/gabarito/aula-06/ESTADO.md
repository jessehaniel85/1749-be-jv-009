# Estado ao fim da Aula 6 — os serviços se acham sozinhos

## O que mudou desde a Aula 5

| Antes (aula-05) | Agora (aula-06) |
|---|---|
| `servicos.cliente.url: http://localhost:8081` no yml | `@FeignClient(name = "cliente-service")` — **nome lógico** |
| `ClienteClient` era uma classe com `RestClient`, `try/catch` e montagem de URI | `ClienteClient` é uma **interface**; o Feign gera a implementação |
| Trocar de máquina = editar yml e reiniciar | Trocar de máquina = **nada**; o Eureka reaprende |
| 3 processos | 4 processos (entra o `discovery-server` na 8761) |

## Como subir (ordem importa muito mais agora)

```bash
# terminal 1 — PRIMEIRO o catálogo, senão os outros sobem reclamando
cd aula-06 && mvn -pl discovery-server spring-boot:run   # http://localhost:8761

# terminais 2, 3, 4
cd aula-06 && mvn -pl cliente-service spring-boot:run    # 8081
cd aula-06 && mvn -pl cotacao-service spring-boot:run    # 8082
cd aula-06 && mvn -pl cambio-service  spring-boot:run    # 8083
```

> Na rede corporativa, acrescente `-s ../../../../ambiente/settings.xml`.

**Abra `http://localhost:8761` e projete na tela.** O painel do Eureka é a melhor parte da
aula: os três nomes aparecendo em *Instances currently registered*, um de cada vez, conforme
você sobe os terminais. Suba o `cambio-service` **por último** e mostre a lista crescendo.

> **Há duas esperas diferentes, e confundi-las gera pânico em aula.** (1) o serviço leva
> alguns segundos para *aparecer no painel* — é o primeiro heartbeat; (2) o `cambio-service`
> leva mais um ciclo para *enxergar* quem apareceu — ele guarda uma cópia local do registro e
> a atualiza a cada `registry-fetch-interval-seconds` (padrão **30s**; baixamos para **5s** no
> `application.yml` justamente para a aula). Ou seja: **ver no painel não é o mesmo que já
> conseguir chamar.** Se chamar `POST /ordens` cedo demais você toma um **503** — mostre,
> espere 5s, repita e funcione. É uma das melhores demonstrações da aula.

## Ponta a ponta

Os cURLs são **idênticos aos da Aula 5** — e esse é o ponto: mudou tudo por dentro,
não mudou nada para quem consome.

```bash
curl -i -X POST http://localhost:8081/clientes -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Souza","cpf":"43488428095","dataNascimento":"1990-05-10",
       "estadoCivil":"SOLTEIRO","sexo":"FEMININO"}'

curl -s http://localhost:8082/cotacoes/EUR

curl -i -X POST http://localhost:8083/ordens -H 'Content-Type: application/json' \
  -d '{"cpf_cliente":"43488428095","tipo_moeda":"EUR",
       "valor_moeda_estrangeira":100.00,"numero_agencia_retirada":"7057"}'
# → 201  { ..., "valor_total_operacao": 658.57, ... }
```

### A demonstração que fecha a aula

1. Derrube o `cliente-service` (`Ctrl+C` no terminal 2) e chame `POST /ordens`:

```
HTTP/1.1 503 Service Unavailable
{ "erro":"Servico indisponivel",
  "mensagem":"Nenhuma instancia registrada no Eureka para o servico procurado. ..." }
```

2. Suba o `cliente-service` **em outra porta**, sem tocar em nada no `cambio-service`:

```bash
mvn -pl cliente-service spring-boot:run -Dspring-boot.run.arguments=--server.port=9091
```

3. Espere o painel do Eureka mostrar a instância nova e repita o `POST /ordens`. **Funciona.**

Para pensar: *na Aula 5, quantos arquivos precisaríamos editar para essa mudança de porta?*
(Um `application.yml` e um restart do `cambio-service`. Aqui: zero.)

E o passo seguinte, se quiser mostrar load balancing: suba **duas** instâncias do
`cotacao-service` (portas 8082 e 9092) e faça 6 chamadas seguidas a `POST /ordens` com o
`logger-level: basic` do Feign ligado — as requisições se alternam entre as duas.

## 🅲 Plano C — rodar **sem** Eureka

Se o Nexus corporativo não resolver os artefatos do Netflix Eureka, ou se você quiser rodar
só três terminais, existe o perfil `plano-c`. **Nenhuma linha de Java muda.**

```bash
# sem o discovery-server; os 3 serviços com o perfil plano-c
mvn -pl cliente-service spring-boot:run -Dspring-boot.run.profiles=plano-c
mvn -pl cotacao-service spring-boot:run -Dspring-boot.run.profiles=plano-c
mvn -pl cambio-service  spring-boot:run -Dspring-boot.run.profiles=plano-c
```

Por que funciona — vale abrir o código e mostrar:

```java
@FeignClient(name = "cliente-service", url = "${servicos.cliente.url:}")
public interface ClienteClient { ... }
```

O `:` no fim de `${servicos.cliente.url:}` é um **valor padrão vazio**. O Spring Cloud OpenFeign
decide assim, em tempo de criação do bean:

| `url` resolvida | Comportamento |
|---|---|
| vazia (perfil padrão) | resolve `name` pelo Eureka + LoadBalancer |
| preenchida (`application-plano-c.yml`) | vai direto na URL, sem consultar o Eureka |

`application-plano-c.yml` faz duas coisas: liga `eureka.client.enabled: false` e define
`servicos.cliente.url` / `servicos.cotacao.url`. Só isso.

**Como comparar em aula:** rode o mesmo `POST /ordens` nos dois modos e mostre que a resposta
é byte a byte igual. A diferença está só em *quem responde a pergunta "onde está o
cliente-service?"* — um arquivo de configuração ou um serviço de registro.

## O que este estado NÃO tem (de propósito)

- **Retry / circuit breaker** — se o vizinho cair no meio, a chamada morre ali.
- **Gateway** — o consumidor externo ainda conhece as três portas.
- **Segurança no Eureka** — o painel está aberto. Em produção, nunca.
- **Código limpo** — `OrdemService` faz validação, cálculo e persistência de uma vez, e
  `TratadorDeErros` importa `feign.*`. Isso é a **Aula 7**, e está anotado no `KANBAN.md`.

## Testes

```bash
cd aula-06 && mvn clean test
```

Duas coisas mudaram nos testes:

1. **Os testes de contexto desligam o Eureka** com
   `@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})`.
   Sem isso, cada `mvn test` tentaria registrar no 8761, esperaria o timeout e demoraria uma
   eternidade. *Teste unitário e de fatia não fala com a rede — nunca.*
2. **`OrdemServiceTest` ficou mais simples**: `ClienteClient` e `CotacaoClient` agora são
   interfaces, e interface é o que o Mockito mocka melhor. Compare com a Aula 5, onde eram
   classes concretas cheias de `RestClient` por dentro.

O `discovery-server` **não tem teste**: um `@SpringBootTest` nele subiria o servidor Eureka
inteiro só para provar que o `@EnableEurekaServer` está escrito. Custo alto, informação zero.
