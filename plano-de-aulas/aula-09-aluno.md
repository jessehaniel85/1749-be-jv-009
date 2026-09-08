# Material do Aluno — Aula 9: Apresentar, defender e fechar o módulo

> **Tempo de leitura:** ~9 min. Este é um **guia prático**, não um capítulo novo. Em três semanas você viu uma API de câmbio nascer como monólito, ganhar testes, crescer, virar três serviços, aprender a se descobrir na rede, ficar limpa e, enfim, extensível. Segunda-feira é a vez do **seu** projeto: ~15 minutos para mostrar o que roda e **defender por que ficou assim**. Leia isto **antes** de montar a apresentação — e volte ao checklist da §4 na véspera, com o repositório aberto.

---

## 1. O que a Aula 9 avalia (e o que ela não avalia)

A regra que organiza tudo: **avaliamos a decisão defendida, não a infraestrutura que coube ao seu time.** Um grupo que rodou tudo em H2, sem Docker, e explicou com clareza por que separou (ou por que **não** separou) os serviços tira nota alta. Um grupo com quatro serviços, Eureka e cinco padrões implementados, mas que não sabe dizer por que usou Strategy ali, tira menos.

Isso muda o que você prepara. Não gaste minutos provando que o ambiente subiu; gaste-os provando que a escolha foi **consciente**. A frase que resume a postura esperada é:

> *"Escolhemos X, abrindo mão de Y, porque no nosso contexto Z pesa mais."*

Decisão sem trade-off declarado é o que a banca mais penaliza — e é também o que separa um dev que **usou** um padrão de um dev que **decidiu** usá-lo.

---

## 2. A estrutura dos seus 15 minutos

O tempo é curto e a arguição vem no fim; não desperdice o começo com apresentação de slides institucionais.

| Etapa | Tempo | O que mostrar |
|---|:---:|---|
| Problema e escopo | 2 min | que sistema é, para quem — o **problema**, não a stack |
| Demo do que roda | 4 min | um fluxo ponta a ponta; declare o perfil de execução e os fallbacks |
| Defesa das decisões | 4 min | arquitetura, testes, SOLID e padrões, **apontando os ADRs** |
| Uso crítico de IA | 1 min | onde a IA ajudou e o que vocês validaram à mão |
| Arguição | 4 min | docente + colegas; **cada integrante responde pelo menos uma** |

Quatro princípios de quem apresenta bem:

- **Comece pelo problema.** "Construímos um controle de reserva de salas que precisa evitar conflito de horário" prende mais que "temos dois serviços Spring Boot".
- **Demonstre um fluxo, não cada tela.** Escolha o caminho que exercita as decisões que você quer defender.
- **Mostre evidência localizável.** Abra o ADR, mostre o teste passando, aponte a classe. A banca acredita no que ela consegue achar no repositório.
- **Antecipe a arguição.** Se você já diz "sabemos que o `if` de tipo aqui viola OCP; deixamos assim porque só existem dois casos e não há previsão de crescer", tirou a pergunta da boca do avaliador — e ganhou o ponto do trade-off declarado.

> **💭 Pare e reflita:** se você tivesse **um minuto só**, qual seria a decisão do seu projeto que mais vale defender? Comece a apresentação garantindo que essa decisão será dita — e não perdida no meio da demo.

---

## 3. O que a banca pergunta

A arguição testa se a decisão foi do grupo e se foi consciente. Treine estas — entenda o raciocínio, não decore a resposta.

**"Por que vocês separaram em serviços? (ou: por que não separaram?)"**
> Modelo: "Separamos porque cadastro e processamento têm ciclos de mudança e perfis de carga diferentes — e cada um tem dono claro. Sabemos o preço: latência de rede, falha parcial e duas bases para manter. Se fossem o mesmo perfil, um **monólito modular** seria melhor, e teríamos feito isso."

**"Qual é a sua pirâmide de testes? O que ela cobre e o que não cobre?"**
> Modelo: "Base de unitários no domínio, alguns testes de controller com MockMvc e um de integração no fluxo principal. Não cobrimos o caminho de erro da integração externa — é a nossa maior lacuna conhecida, e está no `README` como próximo passo."

**"Onde vocês aplicaram SOLID? Me mostre o antes e o depois."**
> Modelo: "Aqui: o serviço dependia da classe concreta do cliente HTTP; extraímos uma interface de domínio e criamos um adaptador. Commit `a1b2c3d`. O ganho foi imediato no teste — passamos a testar a regra com um dublê, sem subir servidor."

**"Por que este padrão, e qual alternativa vocês descartaram?"**
> Modelo: "Strategy, porque a regra varia por tipo e temos três casos com previsão de um quarto. Descartamos `if/else` (obriga a editar código testado a cada caso novo) e tabela de parâmetros no banco (a regra é código, não dado — reavaliaríamos se o negócio pedisse mudança sem deploy)."

**"Se eu pedir um caso novo agora, o que você precisa mudar?"**
> Modelo: "Uma classe nova e uma linha de configuração; nenhum arquivo existente." — **É a melhor resposta possível**, e é verificável ao vivo com `git diff --stat`. Se a sua resposta for "mexo em quatro arquivos", diga isso e explique o porquê; honestidade pontua mais que resposta bonita.

**"Como vocês trabalharam? Quem fez o quê?"**
> Modelo: "Sprints de uma semana com board; papéis rotativos. O histórico de commits mostra a distribuição." — Cuidado: o histórico **é** a evidência. Um repositório com tudo em dois commits na véspera contradiz qualquer discurso de processo.

**"O que a IA fez no projeto, e o que vocês recusaram dela?"**
> Modelo: "Usamos para gerar o esqueleto dos testes e explicar erro de configuração. Recusamos uma sugestão que injetava dependência por campo e outra que criava uma Factory desnecessária — revisamos com os princípios da Aula 7."

A meta-resposta para qualquer pergunta que você não previu: **não existe resposta única certa; existe trade-off bem defendido.** "É o padrão do mercado" nunca é resposta.

> **✍️ Experimente agora:** peça a alguém do grupo que faça três dessas perguntas para você, cronometradas em 1 minuto cada. Se você travar em alguma, essa é exatamente a parte do projeto que você ainda não entendeu — e ainda dá tempo.

---

## 4. Checklist de entrega do repositório

O que a banca não encontra, não pontua — por melhor que esteja na sua cabeça.

- [ ] **`README.md`** — problema, arquitetura em uma figura ou lista, **como rodar**, perfil de execução declarado.
- [ ] **`AVALIACAO.md`** — para **cada critério da rubrica**, onde está a evidência (arquivo, classe, teste ou commit). É o mapa que o avaliador segue.
- [ ] **`docs/adr/`** — um ADR por decisão relevante, com **alternativas rejeitadas** e consequência negativa declarada.
- [ ] **`mvn test` verde** no perfil declarado, e a suíte cobrindo o fluxo principal.
- [ ] **SOLID e padrões localizáveis** — indique os commits "antes/depois" e a classe de cada padrão.
- [ ] **Board/`KANBAN.md`** e **commits distribuídos no tempo** — evidência de processo, não de virada de véspera.
- [ ] **Seção de uso de IA** no `README` — onde usaram, o que revisaram, o que descartaram.

Os dez critérios da rubrica (peso entre parênteses): ágil vivido (10) · decomposição arquitetural (15) · comunicação entre serviços (10) · qualidade e testes (12) · Clean Code (10) · SOLID (12) · design patterns (12) · decisões arquiteturais/ADRs (8) · uso crítico de IA (5) · execução comprovada (6). Cada um vale de **0** (ausente) a **3** (correto, justificado **e** com trade-off declarado). A apresentação e a arguição individual contam à parte, como participação. A tabela completa está no brief do projeto final.

---

## 5. Síntese do módulo em uma página

<details><summary>Ver esquema em texto…</summary>

```
  A1  monólito nasce ágil        Cliente + Kanban + sprint
       │  "e se quebrar?"
  A2  testes                     pirâmide: unitário → controller → integração
       │  "e quando crescer?"
  A3  monólito cresce            Cotação e Ordem, pacotes por domínio
       │  "e quem consome isso?"
  A4  contratos (SOA × REST)     CotacaoProvider: interface + Adapter
       │  "e quando um domínio precisa escalar sozinho?"
  A5  microsserviços             3 serviços, bases segregadas
       │  "e como eles se acham?"
  A6  discovery + Feign          registro + cliente declarativo
       │  "e quando o código endurecer?"
  A7  Clean Code + SOLID         construtor, contratos, abstrações
       │  "e quando a regra variar?"
  A8  design patterns            Strategy, Facade, Singleton, Adapter + ADR
```
</details>

Três ideias sobrevivem ao módulo, e valem mais que qualquer sigla:

1. **Cada passo resolveu uma dor do passo anterior.** Nada aqui foi adotado por ser moderno — e a pergunta certa diante de qualquer novidade continua sendo *"que dor isso resolve, e quanto custa?"*.
2. **Teste é o que compra o direito de mudar.** Sem rede, todo sistema congela: ninguém refatora, ninguém quebra o monólito, ninguém aceita sugestão de IA com segurança.
3. **Decisão não registrada é decisão perdida.** O ADR de cinco linhas de hoje é a resposta que o time terá daqui a dois anos, quando ninguém lembrar por quê.

---

## 6. Ponte com o legado: o que levar para o trabalho na segunda-feira

Nada aqui exige um projeto novo. Cinco ações que cabem no seu sistema atual, em ordem de esforço:

1. **Troque `@Autowired` de campo por construtor** em uma classe. É mecânico, seguro e imediatamente testável.
2. **Extraia constantes** dos números mágicos de uma classe (tamanhos, escalas, limites). Cinco minutos, e o próximo a ler agradece.
3. **Escreva um teste de caracterização** para o método que ninguém quer tocar. Ele registra o comportamento atual e é o que vai permitir mexer ali algum dia.
4. **Escolha um `if/else` de tipo** que cresce a cada produto novo e extraia **um** ramo para uma estratégia. Um só, com teste. Depois o próximo.
5. **Escreva um ADR** para uma decisão que o time já tomou e nunca registrou. Contexto, decisão, alternativas, consequências — cinco linhas.

> **🎬 Imagine o cenário:** daqui a seis meses entra alguém no seu time. Essa pessoa vai abrir o repositório e tentar entender por que as coisas são como são. Tudo o que você fizer desta lista é uma mensagem para ela — e, com boa chance, para você mesmo, num plantão de madrugada.

---

## 7. IA & agentes hoje: o papel do dev intermediário

Fecha o módulo a pergunta que ronda a profissão: se a IA escreve o código, o que sobra?

O que ela **acelera** é real e você já sentiu: esqueleto de classe, teste inicial, explicação de erro, tradução de um trecho legado. Isso vira commodity, e quem não usar vai ficar mais lento que o vizinho.

O que ela **não** decide é exatamente o que este módulo treinou:

- **Onde cortar o sistema.** A fronteira depende do negócio, dos times e do perfil de carga — coisas que não estão no seu repositório.
- **Se o padrão se paga.** O modelo sugere Strategy com entusiasmo; decidir que aqui um `switch` é mais legível é julgamento.
- **O que a rede de testes precisa cobrir.** "Os testes passaram" só vale o que os testes valem.
- **Assumir o risco.** Quem responde por uma ordem calculada errado é uma pessoa, não um modelo.

A conclusão é contraintuitiva e vale levar: **quanto mais barato fica escrever código, mais caro fica escrever o código errado** — e mais valioso fica quem sabe especificar, revisar e decidir. Para o dev intermediário, o deslocamento é claro: menos digitação, mais **especificação, revisão e decisão registrada**. As três coisas que você praticou nas últimas três semanas.

---

## Para ir além

- **Robert C. Martin**, *Código Limpo* e *Arquitetura Limpa* — a dupla que sustenta as Aulas 7 e 9.
- **GoF**, *Padrões de Projeto* + **Refactoring Guru** (`https://refactoring.guru/pt-br/design-patterns/catalog`) — catálogo para consultar no trabalho.
- **Martin Fowler**, *Refactoring* (2ª ed.) e o artigo *The Practical Test Pyramid* — as Aulas 2 e 7 na fonte.
- **Michael Nygard**, *Documenting Architecture Decisions* — o formato ADR, do autor original.
- **Susan J. Fowler**, *Microsserviços Prontos para a Produção* (Novatec) — o que vem depois de "consegui separar": padronização, prontidão operacional e o custo real de N serviços.

> **Depois do módulo — BE-JV-013, Fundamentos de Segurança para Aplicações:** você agora tem serviços que conversam entre si e expõem API para o mundo. Falta a pergunta que este módulo deliberadamente não fez: **quem pode chamar, com que credencial, e o que acontece se o dado vazar?** Autenticação, autorização, tratamento de segredos e as falhas mais comuns em aplicação web são o próximo degrau da trilha — e, num sistema financeiro, não são um detalhe: são requisito.
