# Requisitos — API de Câmbio (ordem de compra de moeda estrangeira)

Pacote de **elicitação de requisitos** do projeto guiado do módulo **BE-JV-009** (turma 1749, Nível II — Intermediário). Simula a coleta de requisitos de um sistema real e serve de **entrada** para o projeto que construímos ao vivo, aula a aula.

## O sistema, em uma frase

Uma API que permite a um cliente **se cadastrar**, **consultar a cotação** de uma moeda estrangeira e **registrar uma ordem de compra** dessa moeda, para **retirada em uma agência**. Ela **não movimenta dinheiro**: o pagamento e a entrega das cédulas acontecem no caixa da agência, na retirada.

## O que tem neste pacote

| Arquivo | O que é |
|---|---|
| `personas.md` | Ficha de cada participante das reuniões (papel, objetivos, jeito de falar, vieses). |
| `sessao-1-kickoff.md` | Transcrição — contexto, problema e escopo (o que entra, o que fica de fora). |
| `sessao-2-cadastro-e-ordem.md` | Transcrição — campos do cliente, CPF, agência de retirada, moedas, limites, erros e mensagens. |
| `sessao-3-cotacao-e-integracoes.md` | Transcrição — de onde vem a cotação, provedor externo, congelamento, arredondamento, auditoria e retenção. |
| `user-stories.md` | Documento compilado pela Product Owner — **primeira fonte de verdade**. |

> O pacote inclui as **transcrições na íntegra** das três reuniões (Product Owner + stakeholders) e o documento de **user stories** que a PO compilou ao final.

## Como usar (leia antes de começar)

1. **As `user-stories.md` são a primeira fonte de verdade.** Comecem por elas. É o documento que a PO entregou ao time como ponto de partida do desenvolvimento, e é dele que sai o backlog no Kanban.

2. **As transcrições estão aqui para vocês recorrerem a elas.** Use as três sessões para:
   - **entender o contexto** — por que cada decisão foi tomada, qual dor de negócio ela resolve, o que cada área precisa;
   - **resolver erros e omissões da PO** — a compilação foi feita às pressas (a própria PO avisa isso, mais de uma vez). Onde a user story estiver vaga, incompleta ou em conflito, **a transcrição manda**. Quem disse, em qual reunião, é o que vale.

3. **Em caso de divergência entre a user story e a transcrição, a transcrição prevalece** — ela é o registro do que os stakeholders efetivamente pediram. Tratem as user stories como uma boa primeira versão, não como verdade infalível.

> Dica de método: ao ler uma user story, pergunte-se *"isso bate com o que foi dito na reunião correspondente?"*. Vale montar uma pequena lista de correções (um **erratum**) conforme forem encontrando divergências, citando a sessão e o trecho. Requisito vago não se adivinha: **se marca como pergunta ao PO**.

## Relação com o módulo

Este pacote não é enfeite: ele é o insumo dos três eixos do BE-JV-009.

- **Ágil (Aula 1)** — as user stories viram os **cards do Kanban** do projeto guiado. Priorizar, quebrar história grande, distinguir história de negócio de **enabler story** (história técnica que habilita as demais) e mover card na *daily* é o exercício de ágil do módulo. O backlog do `KANBAN.md` sai daqui.
- **Arquitetura (Aulas 3 a 6)** — o arco das histórias descreve a mesma evolução que o código vai sofrer: um **monólito** que cresce por domínios, um **contrato de serviço** para a cotação, a **quebra em serviços** e a **comunicação entre eles**. Cada decisão de projeto deve ser rastreável a uma fala de stakeholder — é o que um ADR faz.
- **Qualidade (Aulas 2, 7 e 8)** — os **critérios de aceite** estão escritos em Dado/Quando/Então justamente para virarem **teste automatizado** quase sem tradução. Um critério que você não consegue transformar em asserção é um critério mal escrito: reescreva-o.

Os detalhes técnicos de implementação (stack, pacotes, perfis de execução, incremento de cada aula) estão no `brief.md` do projeto guiado. **Este pacote é sobre o quê e o porquê — o domínio e os requisitos —, não sobre o como.**

## Importante

- O material é **cliente-agnóstico**: o contratante é sempre "uma instituição financeira". Não há nome de banco real aqui, e não deve haver no código.
- Escreva em português (pt-BR), como o restante do material.
- Não existe "resposta única" para o desenho. O objetivo é praticar **leitura crítica de requisitos** e **rastreabilidade** — ligar cada linha de código a uma decisão, e cada decisão a alguém que a pediu.
