# Kanban — API de Câmbio (estado ao fim da **Aula 8** — projeto concluído)

> O board é movido **ao vivo**, em aula. Este arquivo é o espelho versionado dele.

## DONE

| # | História | Entregue em | Onde olhar |
|---|---|---|---|
| US-01 | Como cliente, quero **me cadastrar** informando nome, CPF, nascimento, estado civil e sexo, para poder comprar moeda. | Aula 1 | `cliente-service` → `ClienteController#cadastrar` |
| US-02 | Como cliente, quero **consultar meu cadastro pelo CPF**, para conferir meus dados. | Aula 1 | `cliente-service` → `ClienteController#buscarPorCpf` |
| US-03 | Como time, queremos uma **suíte de testes na pirâmide** (unidade → fatia web → integração), para refatorar sem medo. | Aula 2 | `src/test` de todos os módulos |
| US-04 | Como cliente, quero **consultar a cotação** de USD/EUR, para decidir se compro hoje. | Aula 3 | `cotacao-service` → `CotacaoController#consultar` |
| US-05 | Como cliente, quero **registrar uma ordem de compra** com retirada em agência e receber o comprovante. | Aula 3 | `cambio-service` → `OrdemController` |
| US-06 | Como time, queremos que a **fonte da cotação seja trocável** (tabela local × provedor externo) sem mudar quem consome. | Aula 4 | `CotacaoProvider` + implementações |
| US-07 | Como time, queremos **quebrar o monólito em 3 serviços com bases segregadas**, para evoluir e escalar cada domínio no seu ritmo. | Aula 5 | os três módulos de serviço |
| US-08 | Como time, queremos que os serviços **se descubram por nome** (sem URL na mão) e que a chamada remota seja **declarativa**. | Aula 6 | `discovery-server` + `@FeignClient` em `ordem/infra` |
| US-09 | Como time, queremos o código **limpo e aderente a SOLID**, sem mudar comportamento, para reduzir custo de manutenção. | Aula 7 | `ValidadorDeOrdem`, `CalculadoraDeOperacao`, `ConsultaCliente`/`ConsultaCotacao`, `*ClientAdapter` |
| US-10 | Como mesa de câmbio, queremos **regra comercial por moeda** (spread de 0,5% no euro, dólar sem spread), com os padrões que sustentem moedas novas sem reabrir código existente. | **Aula 8** | `CalculoOperacaoStrategy` + `CambioFacade` + `CatalogoMoedas*` + `OrdemResponse.Construtor` |

## TO DO

*(vazio — o backlog do projeto guiado terminou)*

Candidatos que ficaram **fora de escopo** e valem como conversa de encerramento:

| Ideia | Por que não entrou |
|---|---|
| Retry + circuit breaker (Resilience4j) | Tema do módulo de mensageria/resiliência, não deste |
| API Gateway | O consumidor ainda conhece as três portas; um gateway resolveria, mas é mais um processo |
| Eventos de domínio (ordem registrada → antifraude) | Precisa de broker → BE-JV-010 |
| Cache de cotação (Decorator sobre `CotacaoProvider`) | Sem requisito de performance que justifique |

## Débito técnico — quitado na Aula 7

| Registrado na Aula 6 | Como foi quitado |
|---|---|
| `TratadorDeErros` importava `feign.*` (infra vazando até a borda) | os `*ClientAdapter` traduzem `FeignException` para exceção de domínio na fronteira |
| `OrdemService` (`dominio`) importava `infra` | as portas `ConsultaCliente`/`ConsultaCotacao` nasceram no `dominio`; `infra` é que passou a depender delas |
| `OrdemService` com 4 responsabilidades | separado em `ValidadorDeOrdem` + `CalculadoraDeOperacao` + `OrdemService` |
| números mágicos (`11`, `4`, `2`) espalhados | `RegrasDeCambio` e `RegrasDeCliente` |

> **Critério de pronto da US-09:** `mvn test` verde **e** nenhum status HTTP diferente do que a
> Aula 6 devolvia. Refatoração que muda comportamento não é refatoração — é mudança disfarçada.

## ⚠️ A US-10 MUDA comportamento — e tudo bem

A US-09 tinha como critério "nenhuma resposta diferente". A US-10 **muda** uma: o total de uma
ordem em EUR passou de `658.57` para `661.86`, por causa do spread de 0,5%.

A diferença entre as duas não é de tamanho, é de natureza:

| | US-09 (Aula 7) | US-10 (Aula 8) |
|---|---|---|
| O que é | refatoração | funcionalidade nova |
| Comportamento | idêntico, por definição | muda, por pedido do negócio |
| Quem pediu | o time | a mesa de câmbio |
| Teste | os antigos continuam valendo | teste novo documenta a regra nova |

Chamar mudança de regra de "refatoração" no daily é como se perde a confiança do PO.

## Regras do board

- Card só sai de **DOING** com **teste verde**.
- Card que virar dúvida de arquitetura vira **ADR** em `docs/adr/`.
- WIP limite 1 por dupla: quem está com dois cards em DOING está com zero.
