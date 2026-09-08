# Plano de Aulas — BE-JV-009 · Escalação Tech 02 · Nível II (Intermediário) · turma 1749

Guia mestre das 9 aulas. Cada `aula-0X.md` (instrutor) e `aula-0X-aluno.md` (aluno) segue o método, a trilha de IA e a estratégia de turma definidos **aqui** — leia este README antes dos planos individuais.

## Calendário (9 × 3h) — turma 1749 · 09/09 → 28/09/2026

> Seg/Qua/Sex, 3h por encontro. Sem feriado no intervalo (7/9 cai antes). Mesmas datas da turma irmã 1751 (BE-JV-010, Nível III). Imagem em `../cronograma.png`. Ordem = planejamento oficial do módulo (cadência 3×/semana: 8 aulas de conteúdo + 1 de devolutiva).

| Aula | Data | Tema | Incremento no projeto guiado (Câmbio) |
|---|---|---|---|
| 1 | Qua **09/09** | **Abertura + Ágil na prática** (Manifesto, Scrum, Kanban) | Monólito nasce: `Cliente` (POST/GET por CPF) com backlog em Kanban |
| 2 | Sex **11/09** | **Qualidade e pirâmide de testes** + o que é arquitetura de software | Testes unitários, de controller e de integração na API |
| 3 | Seg **14/09** | **Arquitetura monolítica** — crescendo o monólito com juízo | Domínios `Cotação` e `Ordem` no mesmo projeto; kickoff dos projetos em grupo |
| 4 | Qua **16/09** | **SOA × REST** — contratos de serviço e integração externa | `CotacaoProvider` (contrato) local × externo + Adapter |
| 5 | Sex **18/09** | **Quebrando o monólito** em microsserviços | `cliente-service`, `cotacao-service`, `cambio-service`, bases segregadas |
| 6 | Seg **21/09** | **Comunicação entre microsserviços** — service discovery (Eureka) + OpenFeign | `discovery-server` + `@FeignClient` (Plano C: `@HttpExchange` + registro estático) |
| 7 | Qua **23/09** | **Clean Code + SOLID** — refatorando sem quebrar | Injeção por construtor, SRP/OCP/LSP/ISP/DIP nos serviços |
| 8 | Sex **25/09** | **Design Patterns** — criacional, estrutural, comportamental | Strategy, Facade, Singleton, Adapter + ADRs |
| 9 | Seg **28/09** | **Apresentações dos projetos em grupo** + devolutiva por rubrica | — |

> Carga: 27h (9×3h). A proposta comercial fala em 20h; a coordenação desta turma é o próprio docente e assume os 27h, como nas turmas 1704/1705/1751.

> **Arquivos:** `aula-0X.md` = plano do instrutor · `aula-0X-aluno.md` = material do aluno (vai para o repositório dos alunos) · `aula-01-slides.pptx` = **único deck** (apresentação do curso). Demais aulas rodam com **material do aluno + IDE compartilhada** (decisão do docente: slide só quando há esquema visual que o texto não resolve).

## Perfil da turma 1749 — a apurar na Aula 1

Hipótese de trabalho (mesmo programa e mesma rede das turmas 1704/1705/1751; **Nível II = Intermediário**, um degrau abaixo da 1751):

- Profissionais da instituição, em **horário de expediente**, dentro da **rede corporativa**, via **Microsoft Teams**.
- **2–5 anos** de experiência, alguns juniores; **legado pesado** (Java 6/EJB 2, JBoss, struts/JSF) e modernização em **Java 17/21 + Spring/Quarkus**. Prática de **Spring Boot desigual**; **Scrum conhecido de ouvir falar**, raramente praticado com rigor; **microsserviços**: já viram, poucos operaram.
- **Copilot com acesso desigual** → atividades de IA opcionais.
- **Apurar nas apresentações de 1 min:** N, anos de experiência, stack atual, "já trabalhou em squad ágil?", "já quebrou um monólito?", "tem Copilot com agente?". Registrar em `transcript/aula-01.md` (formato das turmas anteriores) e ajustar grupos e ritmo.

## Lições das turmas anteriores (1704 · 1705 · 1751) aplicadas aqui

| Sinal | O que fazemos na 1749 |
|---|---|
| Aula 1 corrida (apresentações + checklist de ambiente ao vivo engoliram o hands-on) | **Checklist de ambiente = tarefa prévia (D-3)**; apresentações de **1 min**; o Kanban ao vivo + `POST /clientes` funcionando são **inegociáveis** na Aula 1 |
| Dificuldade com sala invertida nas leituras iniciais | **Onboarding do método** na Aula 1 + **leitura guiada** (3 perguntas-guia por seção, âncora por grupo) nas Aulas 1–3; blocos de leitura curtos no início |
| Pedido de mais **casos reais e decisão arquitetural** (teórico/prático foram os itens mais baixos da rubrica) | **1 estudo de caso real por aula** no bloco de discussão + **ADR ao vivo** a partir da Aula 3 |
| Projetos: participação desigual, over-claiming na auto-avaliação | Brief §6/§7: prazo por committer-date UTC, **arguição individual**, evidência por critério em `AVALIACAO.md` |
| Slides pouco usados | Só o deck da Aula 1; esforço vai para material do aluno + gabarito |
| O que **não** muda | Gabarito aberto desde o início, projeto **dentro** da aula, trilha IA transversal, Plano B pura-JVM, cliente-agnóstico no material do aluno |

## Por que este módulo precisa de adaptação

O módulo foi escrito em 2023/2024 e o banco tem **42 questões (29 Basic, 13 Medium)**. **O módulo oficial diverge internamente:** o `Planejamento/planejamento.md` (grade de 9 aulas + rubrica) pede SOA × REST, Eureka + OpenFeign, recap de Clean Code com injeção por construtor, primeiro teste unitário e implementação de Singleton/Facade/Strategy, mas o `Material do Aluno/` **não cobre** SOA, Eureka/Feign (só um link), Clean Code, injeção nem código de testes/patterns; em compensação traz história das metodologias e Scrumban (não pedidos) e concentra 5 dos 10 capítulos e 26 das 42 questões em ágil. **Decisão:** a grade e a rubrica oficiais são a fonte de verdade; os `aula-0X-aluno.md` desta turma cobrem as lacunas e absorvem o que o material oficial tem. Três decisões de design:

1. **Ágil vivido, não recitado.** O módulo pede Manifesto/Scrum/Kanban na Aula 1. Em vez de expor, **operamos**: o projeto guiado tem backlog, board e sprints de 1 semana (3 aulas); cada aula abre com uma *daily* de 3 min e cada semana fecha com *review + retro* de 10 min. O aluno sai tendo **feito** Scrum-lite, não ouvido falar.
2. **Arquitetura como jornada, não catálogo.** Monólito → SOA/REST → microsserviços → discovery é contado como **evolução de um mesmo sistema** com decisões registradas (ADR). A pergunta de cada aula é "**o que dói** no estado atual e **o que custa** mudar", não "qual é o estilo da moda". Inclui explicitamente **quando não quebrar** o monólito.
3. **Qualidade como refatoração segura.** Testes (Aula 2) entram cedo justamente para que SOLID (Aula 7) e Patterns (Aula 8) sejam **refatorações com rede de proteção**. Clean Code/SOLID já apareceram em módulos anteriores da trilha → revisão rápida de SRP/OCP e **profundidade em LSP, ISP e DIP** (injeção por construtor), como o planejamento oficial recomenda.

## Método Ada por aula (PBL + Sala de Aula Invertida adaptada) — molde de 180 min

> A inversão é **adaptada ao contexto corporativo**: assume-se que **o aluno não estuda fora do expediente**. A "pré-aula" é mínima e opcional; **o tempo de estudo/projeto acontece dentro da aula** (2ª metade). Deliberado — e protege o NPS.

| Bloco | Tempo | O que acontece |
|---|---|---|
| **Provocação (sala invertida lite)** | D-1, ~5 min, opcional | 1 pergunta + 1 link curto no Teams. Quem não viu, a abertura cobre. |
| **0. Daily do projeto guiado** | 0–5 | 3 perguntas do Scrum sobre o board do Câmbio (o instrutor modela; da Aula 3 em diante, um aluno conduz). |
| **1. Problema (PBL)** | 5–20 | Problema gerador **real**. Plenária/breakout: *"como vocês resolvem isso hoje?"* Coleta de hipóteses. |
| **2. Discussão de alto nível** | 20–45 | Trade-offs, **estudo de caso real**, **ponte do legado** (EJB/JBoss/batch/mainframe), ponte da modernização. "❓ Pergunte à turma" a cada ponto. |
| **3. Solução possível (ao vivo)** | 45–90 | Live/mob coding no **projeto Câmbio**, incremento da aula, **movendo cards do Kanban**. Ângulo IA/Agentes entra aqui. ADR ao vivo quando há decisão. |
| *Intervalo* | 90–100 | — |
| **4. Desafio de evolução (studio)** | 100–150 | Alunos **estendem** o que fizemos, em **mob guiado**. Trilha **base** (todos consolidam) e **aprofundamento** (opcional). |
| **5. Tempo de projeto em grupo** | 150–175 | Breakout rooms: grupos avançam o **projeto final** em aula. |
| **6. Fechamento + gancho NPS** | 175–180 | Síntese de 1 frase + *"o que você leva hoje"* + provocação da próxima. Nas Aulas 3, 6 e 8: **review + retro** de 10 min (fecha a sprint). |

Se **breakout rooms** não existirem: blocos 4 e 5 viram **mob programming guiado** + canais por grupo.

### Sprints do módulo (Scrum-lite vivido)

| Sprint | Aulas | Meta | Cerimônias |
|---|---|---|---|
| 1 | 1–3 | Monólito com Cliente, Cotação e Ordem, testado | Planning na A1 · dailies · **review + retro na A3** |
| 2 | 4–6 | Serviços com contratos, quebra e comunicação | Planning na A4 · dailies · **review + retro na A6** |
| 3 | 7–8 (+9) | Código limpo, SOLID, patterns; projetos finais entregues | Planning na A7 · dailies · **review + retro na A8**; A9 = review dos projetos |

## Trilha transversal IA/Agentes (resumo)

| Aula | Tema clássico | Ângulo IA/Agentes |
|---|---|---|
| 1 | Ágil, Scrum, Kanban | IA muda o **ciclo**: gerar mais rápido exige **priorizar e revisar melhor**; backlog escrito como *spec* para agentes; o que o Manifesto diz sobre "software funcionando" quando quem digita é o agente |
| 2 | Pirâmide de testes | Teste é o **contrato** que valida código gerado por IA; IA gerando casos de teste (e por que revisar); *evals* como pirâmide para LLMs |
| 3 | Monólito | **Monólito modular é ótimo para agentes** (contexto único, refatoração segura); *context engineering* = escolher o que o agente vê do repositório |
| 4 | SOA × REST, contratos | Contrato de serviço = **tool definition** para um agente; OpenAPI como linguagem comum humano↔agente; ACL para APIs de LLM |
| 5 | Microsserviços | Sistemas multiagente **são** microsserviços (fronteira, dono, falha parcial); custo operacional de N serviços × N agentes |
| 6 | Discovery, Feign | **Registro de ferramentas/agentes** (MCP, A2A) = service discovery; cliente declarativo = *tool use* declarativo |
| 7 | Clean Code, SOLID | Código legível é **código que o agente edita sem quebrar**; SOLID como guia de *prompt* de refatoração; revisar o que o Copilot gerou com os 5 princípios |
| 8 | Design Patterns | Patterns como **vocabulário compartilhado com a IA** ("aplique Strategy aqui"); *agentic patterns* (tool use, reflection, planner-executor) como os novos GoF |
| 9 | — | Como a IA muda o **papel do dev intermediário**: menos digitação, mais especificação, revisão e decisão |

## Estratégia para turma mista (legado × moderno, com juniores)

- **Ponte do legado** — ancorar cada conceito em algo que já viveram: cascata e "documento de 200 páginas" ↔ Manifesto; EJB/EAR único ↔ monólito; web service SOAP/ESB ↔ SOA; `new` dentro do service e `@Autowired` em campo ↔ DIP; `if/else` de tipo ↔ Strategy.
- **Ponte da modernização** — fechar mostrando que é o que os novos serviços Spring/Quarkus (Java 17/21) da instituição precisam: bases segregadas, contratos explícitos, testes que permitem refatorar.
- **Trilha base × aprofundamento** no studio: base = fazer funcionar, todos chegam (mob guiado); aprofundamento = trade-off/otimização, opcional.
- **Banco de 42 questões** = consolidação formativa **via LMS** (confirmar acesso antes da Aula 1). Mapa por aula abaixo.
- **Debates de alto nível** (níveis diferentes contribuem): "Scrum de fachada", "monólito modular × microsserviços", "REST não é SOA", "over-engineering com patterns", "cobertura alta ≠ teste bom".

## Exercícios do banco por aula (consolidação formativa, via LMS)

| Aula | Questões `be_jv_009_XX` | Tema |
|---|---|---|
| 1 | 01–26 | história das metodologias, Manifesto e ágil, Scrum (papéis, cerimônias), Kanban — aplicar entre as Aulas 1 e 2 |
| 2 | 42 | teste de software |
| 3 | 27–30 | introdução à arquitetura de software |
| 4 | 36–37 | padrões de organização de componentes |
| 5 | 38–40 | síncrono × assíncrono, transições monólito → microsserviços |
| 6 | 41 | service discovery |
| 7 | 31–33 | SOLID |
| 8 | 34–35 | Design Patterns |

## Os dois projetos (apresentados na Aula 1)

- **Guiado em aula** — **API de Câmbio** (ordem de compra de USD/EUR), construída incrementalmente (ver `../projetos/projeto-guiado-cambio/brief.md`). É o que demonstramos ao vivo, com Kanban vivo.
- **Final em grupo** — **tema livre** definido por cada grupo, avaliado por **critérios** (ver `../projetos/projeto-final-grupo/`). O Câmbio é só **sugestão de referência**. Grupos de 4 (`teto(N/4)`).

## Perfil de execução — Plano B é o piso

Rede corporativa **sem Docker e sem sandbox** (confirmado pelas turmas anteriores). Este módulo é **pouco dependente de infra**: só JVM, Maven, H2. O plano de cada aula roda em pura-JVM.

- **Plano B (o plano):** Spring Boot + H2 + (Aula 6) Spring Cloud Eureka/OpenFeign via Nexus. Roda em qualquer máquina com Java 21 + Maven + Nexus.
- **Plano C (fallback conceitual):** se o Nexus **não publicar Spring Cloud**, a Aula 6 roda com `RestClient` + `@HttpExchange` e um registro estático de URLs em `application.yml`; Eureka é demonstrado pelo instrutor (gravação/tela) e explicado como conceito. **O aprendizado do padrão não depende da infra.**
- **Provedor externo de cotação** (awesomeapi) é inacessível da rede → o projeto já nasce com provedor **local** (ver brief). Fora da rede, o instrutor pode ligar o provedor externo por propriedade.

> **Risco aberto:** resolução de `spring-cloud-starter-netflix-eureka-*` e `spring-cloud-starter-openfeign` no **Nexus** — ver `../ambiente/checklist-semana-0.md`, seção 3. Decide B × C para a Aula 6 (21/09); fechar até a Aula 4.
