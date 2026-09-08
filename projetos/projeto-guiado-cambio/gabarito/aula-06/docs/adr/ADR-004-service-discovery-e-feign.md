# ADR-004 — Service discovery com Eureka e cliente declarativo com OpenFeign

- **Status:** aceita (com Plano C documentado)
- **Data:** Aula 6
- **Decisores:** time do projeto guiado (turma 1749)
- **Depende de:** ADR-003 (quebra do monólito)

## Contexto

A Aula 5 deixou uma dívida explícita no `application.yml` do `cambio-service`:

```yaml
servicos:
  cliente: { url: http://localhost:8081 }
  cotacao: { url: http://localhost:8082 }
```

Isso funciona com **um** ambiente, **uma** instância por serviço e **zero** mudança de
endereço. Nenhuma das três hipóteses sobrevive fora do notebook:

- subir uma segunda instância do `cotacao-service` não ajuda em nada — a URL aponta para uma só;
- mudar a porta de um serviço obriga a **editar e reiniciar** quem o consome;
- cada ambiente (dev, homolog, prod) vira mais um bloco de configuração para manter sincronizado.

Havia também uma dívida no código: `ClienteClient` e `CotacaoClient` eram ~30 linhas cada,
95% idênticas, misturando *o que chamar* com *como chamar*.

## Decisão

**1. Service discovery com Netflix Eureka**, no módulo `discovery-server` (porta 8761).
Cada serviço se registra pelo `spring.application.name`; quem consome pergunta pelo nome.

**2. Chamada remota declarativa com OpenFeign**: `ClienteClient` e `CotacaoClient` viram
interfaces anotadas. O `spring-cloud-starter-loadbalancer` escolhe uma instância entre as
que o Eureka devolveu.

**3. `spring-cloud-dependencies` 2025.0.3** como BOM no `dependencyManagement` do POM pai,
que é o trem compatível com Spring Boot 3.5.x.

**4. Plano C no mesmo código**: os `@FeignClient` declaram `url = "${servicos.cliente.url:}"`
(valor padrão **vazio**). Vazio → resolve pelo Eureka. Preenchido (perfil `plano-c`) → vai
direto na URL. A mesma anotação atende aos dois modos, **sem `if`, sem duas implementações,
sem `@Profile`**.

## Por que Eureka

| Alternativa | Por que não aqui |
|---|---|
| **Consul / etcd** | Melhores em produção (KV, health checks ricos, multi-datacenter). Mas exigem **processo externo** — na prática, Docker. Está fora do escopo desta turma (ADR-003, restrição de ambiente). |
| **Kubernetes Services** | É a resposta certa em cluster: o discovery vem do orquestrador e a aplicação nem sabe. Não temos cluster nesta turma. |
| **DNS + balanceador** | Funciona, mas empurra o problema para a infra e some com a discussão que a aula quer ter. |
| **Eureka** | **Roda como app Spring Boot** (`mvn spring-boot:run`), sem infra externa. Didaticamente é o melhor: o registro é visível num painel HTML que dá para projetar. |

O ponto pedagógico: **discovery é um conceito, Eureka é uma implementação.** Em k8s o mesmo
`@FeignClient(name = "cliente-service")` funciona trocando o starter — porque o que o código
conhece é um *nome*, não um *endereço*.

## Por que OpenFeign (e não `RestClient` com `@LoadBalanced`)

Ambos resolvem. `RestClient` + `@LoadBalanced` seria menos uma dependência.
Escolhemos Feign por três razões:

1. **A interface é o contrato.** Ela documenta o que consumimos do vizinho num arquivo que
   cabe na tela — e serve de base para *contract testing* depois.
2. **Some o código repetido.** Duas classes de ~30 linhas viraram duas interfaces de 4.
3. **Testabilidade.** Mockar uma interface é trivial; mockar uma classe que embrulha
   `RestClient` exige mais cerimônia. Compare `OrdemServiceTest` da Aula 5 com o da Aula 6.

Alternativa moderna considerada: **`@HttpExchange`** (interfaces HTTP nativas do Spring 6, sem
Spring Cloud). É para onde o ecossistema caminha e é o que usaríamos num projeto novo hoje.
Ficou de fora porque o módulo precisa mostrar Spring Cloud, e porque a integração de
`@HttpExchange` com o LoadBalancer ainda exige mais configuração manual que o Feign.

## Consequências

**Ganhamos:** endereço deixou de ser configuração; múltiplas instâncias passaram a ser
possíveis de graça (round-robin do LoadBalancer); o código do cliente encolheu ~85%.

**Pagamos:**

- **Mais um processo.** São quatro terminais em aula. Se o `discovery-server` cair, os
  serviços já registrados continuam funcionando com o **cache local** do registro — o Eureka
  é AP no teorema CAP, prioriza disponibilidade. Bom argumento de aula.
- **Nova classe de erro.** `503 "No instances available"` não existe em chamada com URL fixa.
  Foi tratado explicitamente no `TratadorDeErros`, com mensagem **diferente** da de "vizinho
  não respondeu": *não ter telefone* e *ninguém atender* são problemas distintos.
- **Latência de registro.** Uma instância recém-subida demora ~10s para ser vista. Ajustamos
  `lease-renewal-interval-in-seconds: 5` **só para a aula**; em produção o padrão (30s) existe
  para não afogar o servidor de heartbeats.
- **Acoplamento a `feign.*` na borda.** `TratadorDeErros` agora importa `feign.FeignException`.
  É infraestrutura vazando até o controller — dívida registrada no `KANBAN.md` e alvo da
  **US-09** (ADR-005: DIP + Adapter).
- **Risco de ambiente.** É o único ponto do módulo que depende de artefatos que podem não
  existir no Nexus corporativo. Daí o Plano C.

## Sobre o Plano C

Não é "o jeito errado guardado no bolso": é **a mesma arquitetura com outra fonte de
endereço**. O desenho de dependências é idêntico; muda quem responde "onde está o
cliente-service?" — um arquivo de configuração ou um serviço de registro.

Manter os dois no mesmo código, sem ramificação, foi decisão consciente. A alternativa
(duas implementações de client, escolhidas por `@Profile`) dobraria o código de integração
para variar exatamente **uma** informação.

Como validar antes da aula: `mvn -s ambiente/settings.xml dependency:get -Dartifact=org.springframework.cloud:spring-cloud-starter-netflix-eureka-server:4.3.0`.
Falhou? A Aula 6 roda em `plano-c` e o Eureka fica na conversa, não no teclado.
