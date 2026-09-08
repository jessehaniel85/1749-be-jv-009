# Kanban — API de Câmbio (estado ao fim da **Aula 5**)

> O board é movido **ao vivo**, em aula. Este arquivo é o espelho versionado dele.

## DONE

| # | História | Entregue em | Onde olhar |
|---|---|---|---|
| US-01 | Como cliente, quero **me cadastrar** informando nome, CPF, nascimento, estado civil e sexo, para poder comprar moeda. | Aula 1 | `cliente-service` → `ClienteController#cadastrar` |
| US-02 | Como cliente, quero **consultar meu cadastro pelo CPF**, para conferir meus dados. | Aula 1 | `cliente-service` → `ClienteController#buscarPorCpf` |
| US-03 | Como time, queremos uma **suíte de testes na pirâmide** (unidade → fatia web → integração), para refatorar sem medo. | Aula 2 | `src/test` de todos os módulos |
| US-04 | Como cliente, quero **consultar a cotação** de USD/EUR, para decidir se compro hoje. | Aula 3 | `cotacao-service` → `CotacaoController#consultar` |
| US-05 | Como cliente, quero **registrar uma ordem de compra** com retirada em agência e receber o comprovante. | Aula 3 | `cambio-service` → `OrdemController` |
| US-06 | Como time, queremos que a **fonte da cotação seja trocável** (tabela local × provedor externo) sem mudar quem consome. | Aula 4 | `CotacaoProvider` + `CotacaoLocalProvider` / `CotacaoExternaProvider` |
| US-07 | Como time, queremos **quebrar o monólito em 3 serviços com bases segregadas**, para evoluir e escalar cada domínio no seu ritmo. | **Aula 5** | `cliente-service`, `cotacao-service`, `cambio-service` |

## TO DO

| # | História | Prevista |
|---|---|---|
| US-08 | Como time, queremos que os serviços **se descubram por nome** (sem URL na mão) e que a chamada remota seja **declarativa**. | Aula 6 |
| US-09 | Como time, queremos o código **limpo e aderente a SOLID**, sem mudar comportamento, para reduzir custo de manutenção. | Aula 7 |
| US-10 | Como time, queremos **padrões de projeto explícitos** (Strategy, Facade, Adapter, Singleton) onde eles pagam o próprio preço. | Aula 8 |

## Regras do board

- Card só sai de **DOING** com **teste verde**.
- Card que virar dúvida de arquitetura vira **ADR** em `docs/adr/`.
- WIP limite 1 por dupla: quem está com dois cards em DOING está com zero.
