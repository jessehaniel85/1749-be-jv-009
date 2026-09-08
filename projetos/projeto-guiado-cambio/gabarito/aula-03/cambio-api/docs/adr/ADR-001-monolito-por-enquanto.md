# ADR-001 — Monólito modular por enquanto

**Status:** aceito · **Data:** Aula 3 · **Decide:** time do projeto guiado

## Contexto

A API já tem três domínios — `cliente`, `cotacao` e `ordem` — e o registro de uma ordem depende dos outros dois: precisa confirmar que o CPF existe e pegar a cotação vigente.

A tentação, com "microsserviços" no nome do módulo, é separar os três agora. Mas nesta altura:

- os três domínios mudam **juntos** (todo ajuste no comprovante mexe em cotação e cliente);
- a fronteira entre eles ainda está sendo descoberta — separar cedo significa acertar o desenho errado em três repositórios em vez de um;
- não há requisito de escala, time ou implantação que justifique o custo de rede, serialização e falha parcial.

## Decisão

Manter **um único deployable** (`cambio-api`), com **separação por pacote de domínio** (`cliente`, `cotacao`, `ordem`) e, dentro de cada um, por camada (`api`, `dominio`, `infra`).

`OrdemService` consome `ClienteService` e `CotacaoService` por **injeção direta** — chamada de método, mesma JVM, mesma transação.

A regra que sustenta a decisão: **um domínio só conversa com outro pela porta da frente** (o serviço), nunca pelo repositório alheio. É essa disciplina que torna a separação da Aula 5 um recorte, e não uma cirurgia.

## Consequências

**Boas**
- Uma transação cobre a operação inteira: ou a ordem é registrada, ou nada acontece. Sem SAGA, sem compensação.
- Um build, um deploy, um log. O erro aparece inteiro no stack trace.
- Refatorar a fronteira custa um *move class*.

**Ruins / a pagar depois**
- Nada impede tecnicamente que alguém injete `ClienteRepository` dentro de `OrdemService`. A fronteira é **acordo**, não compilador — e por isso precisa de revisão de código.
- Escala é do monólito inteiro: se a consulta de cotação virar gargalo, sobe-se tudo junto.
- Uma falha em qualquer domínio derruba o processo inteiro.

## Quando revisitar

Na **Aula 5**, quando o exercício for justamente pagar esse preço: bases segregadas, chamadas HTTP, falha parcial. A pergunta a fazer lá é se o problema mudou — ou se só quisemos praticar.
