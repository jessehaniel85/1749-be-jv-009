# Gabarito evolutivo — API de Câmbio (BE-JV-009)

Cada pasta é o estado **completo, independente e compilável** do projeto ao fim de uma aula.
Não há symlink nem branch: cada estado é um projeto Maven inteiro, pronto para abrir na IDE
ou **colar se o live coding travar** (rede do Teams).

Até a Aula 4 o projeto é um **monólito** (`aula-0X/cambio-api`); a partir da Aula 5 é um
**Maven multi-módulo** com um serviço por pasta.

O gabarito é **liberado no início do módulo**, de propósito: é material de dissecação
("por que assim? que alternativa havia?"), não um prêmio no fim.

> **Sem Docker, sem broker, sem banco externo.** Só Java 21, Maven, H2 em memória — e, a
> partir da Aula 6, o Eureka, que é apenas mais um app Spring Boot.

## Os oito estados

| Estado | O que entra | Testes | Histórias fechadas |
|---|---|---|---|
| [`../inicio/cambio-api`](../inicio/cambio-api) | esqueleto com `// TODO` | — | nenhuma |
| [`aula-01/cambio-api`](aula-01/cambio-api) | monólito, domínio Cliente, validação, `@RestControllerAdvice` | — | US-01, US-02 |
| [`aula-02/cambio-api`](aula-02/cambio-api) | pirâmide de testes (unitário, fatia web, integração) | 12 | + US-03 |
| [`aula-03/cambio-api`](aula-03/cambio-api) | domínios Cotação e Ordem, ADR-001 | 27 | + US-04, US-05 |
| [`aula-04/cambio-api`](aula-04/cambio-api) | contrato `CotacaoProvider` (local × externo), Adapter, ADR-002 | 37 | + US-06 |
| [`aula-05`](aula-05) | **quebra do monólito** em 3 serviços, bases H2 segregadas, `RestClient` com URL fixa, ADR-003 | 39 | + US-07 |
| [`aula-06`](aula-06) | **Eureka + OpenFeign** (+ `discovery-server`), Plano C sem Eureka, ADR-004 | 40 | + US-08 |
| [`aula-07`](aula-07) | **Clean Code + SOLID** (SRP, OCP, LSP, ISP, DIP), `docs/antes-depois-solid.md`, ADR-005 | 58 | + US-09 |
| [`aula-08`](aula-08) | **Design Patterns**: Strategy, Facade, Singleton, Adapter, Builder — ADR-006, ADR-007 | 75 | + US-10 |

## Como rodar

```bash
export JAVA_HOME=$HOME/.sdkman/candidates/java/21-open   # Java 21
```

**Aulas 1–4** (monólito, tudo na porta 8080):

```bash
cd aula-03/cambio-api
mvn clean test
mvn spring-boot:run                 # http://localhost:8080
```

**Aulas 5–8** (multi-módulo, um terminal por serviço):

```bash
cd aula-06
mvn clean test                      # a suíte dos módulos todos
mvn -pl cliente-service spring-boot:run
```

### Rede corporativa (Nexus)

Se o Maven Central estiver bloqueado, acrescente `-s <caminho>/ambiente/settings.xml` a
**todo** comando `mvn`. O número de `../` depende de onde você está:

```bash
# de aula-05/ .. aula-08/  (4 níveis até a raiz do módulo)
mvn -s ../../../../ambiente/settings.xml clean test

# de aula-01/cambio-api .. aula-04/cambio-api  (5 níveis)
mvn -s ../../../../../ambiente/settings.xml clean test
```

## Ordem de subida dos serviços

A ordem **importa** a partir da Aula 5. Um terminal para cada linha:

| Aulas | Ordem | Portas |
|---|---|---|
| 1–4 | `cambio-api` | 8080 |
| 5 | `cliente-service` → `cotacao-service` → `cambio-service` | 8081, 8082, 8083 |
| 6–8 | **`discovery-server`** → `cliente-service` → `cotacao-service` → `cambio-service` | **8761**, 8081, 8082, 8083 |

```bash
cd aula-08
mvn -pl discovery-server spring-boot:run   # painel do Eureka em http://localhost:8761
mvn -pl cliente-service  spring-boot:run
mvn -pl cotacao-service  spring-boot:run
mvn -pl cambio-service   spring-boot:run   # por último: é ele que consome os outros
```

> Nas Aulas 6–8, o `cambio-service` só **enxerga** um vizinho recém-subido depois de um ciclo
> de `registry-fetch-interval-seconds` (ajustado para 5s neste gabarito). Ver no painel do
> Eureka não é o mesmo que já conseguir chamar — e a espera é uma boa demonstração de aula.

**Plano C (Aulas 6–8, sem Eureka):** três terminais, sem o `discovery-server`:

```bash
mvn -pl <modulo> spring-boot:run -Dspring-boot.run.profiles=plano-c
```

Nenhuma linha de Java muda entre os dois modos — só a origem do endereço. Detalhes em
`aula-06/ESTADO.md`.

## Onde olhar em cada pasta

| Arquivo | O que traz |
|---|---|
| `README.md` | o que é aquele estado, endpoints, estrutura |
| `ESTADO.md` | o que funciona, o que ainda não existe, cURLs, perguntas para a turma |
| `KANBAN.md` | o board na posição daquela aula (TO DO / DOING / DONE) |
| `docs/adr/` | decisões de arquitetura, a partir da Aula 3 |
| `docs/contrato-cotacao.md` | contrato do serviço de cotação (Aula 4) |
| `docs/antes-depois-solid.md` | os 5 princípios com trecho antes/depois real (Aulas 7–8) |
| `docs/patterns-no-projeto.md` | pattern → classe → por quê, e os que **não** usamos (Aula 8) |

## O que está verificado

| Item | Status |
|---|---|
| `mvn clean test` verde nas 8 aulas | ✅ |
| Spring Boot **3.5.9** · Spring Cloud **2025.0.3** · Java **21** | ✅ combinação verificada com download real |
| Eureka + OpenFeign em runtime (4 processos, fluxo ponta a ponta) | ✅ `POST /ordens` 201, 404/422/400/503 conferidos com cURL |
| `data.sql` semeando USD 5.4321 / EUR 6.5857 | ✅ teste de integração |
| Contrato snake_case de `POST /ordens` | ✅ teste de controller (`$.valor_total_operacao` etc.) |
| Provedor de cotação **local** | ✅ padrão, testado |
| Provedor de cotação **externo** (awesomeapi) | ⚠️ código e Adapter testados em unidade; a chamada HTTP real **não** foi exercitada (rede corporativa bloqueia) |
| Plano C (`-Dspring-boot.run.profiles=plano-c`) | ⚠️ configuração escrita e revisada; não exercitada em runtime |
| Resolução dos artefatos Spring Cloud **no Nexus Caixa** | ❌ pendência de ambiente — ver `ambiente/checklist-semana-0.md`, seção 3 |

## Regras do gabarito

- **Um estado, um projeto.** Copiar `aula-07/` para qualquer lugar e rodar funciona.
- **Sem cliente no código.** O domínio é "uma instituição financeira" — o material é reaproveitável.
- **Identificadores em português.** `ClienteService`, `OrdemDeCompra`, `valorTotalOperacao`.
  Só o vocabulário de framework fica em inglês.
- **Sem Lombok.** O curso escreve construtores e getters — e discute o custo disso.
- **DTOs são `record`.** Imutáveis, sem cerimônia.

## ⚠️ Uma mudança de comportamento entre as aulas

Das Aulas 3 a 7, uma ordem de **100,00 EUR a 6,5857** dá `valor_total_operacao = 658.57` —
o payload de exemplo do `brief.md`.

Na **Aula 8**, o Strategy do euro introduz um **spread comercial de 0,5%** e o mesmo pedido
passa a dar **`661.86`**. O dólar continua sem spread.

Não é inconsistência: é a diferença, deliberada e didática, entre a **Aula 7 (refatoração —
comportamento idêntico, por definição)** e a **Aula 8 (funcionalidade nova — o negócio pediu
uma regra)**. Está explicada em `aula-08/ESTADO.md` e em `aula-08/docs/adr/ADR-006-strategy-por-moeda.md`.
