# ADR-002 — Contrato único para o provedor de cotação

**Status:** aceito · **Data:** Aula 4 · **Decide:** time do projeto guiado

## Contexto

A cotação "de verdade" vem de um provedor externo (awesomeapi). Só que:

- a **rede corporativa bloqueia sites externos**, então em sala a chamada simplesmente não completa;
- mesmo com internet, depender de um terceiro para a aula funcionar é frágil: fora do ar, mudou o JSON, ficou lento;
- por outro lado, ensinar câmbio com um valor fixo chumbado no código esconde justamente o problema interessante — integração com um sistema que não é nosso e que muda quando quer.

Havia três saídas na mesa:

1. **chumbar o valor** em uma constante — simples, e ensina nada;
2. **chamar direto a awesomeapi** de dentro do `CotacaoService` — realista, e quebra na primeira aula sem internet;
3. **inverter a dependência**: o domínio declara o contrato, a infra fornece implementações.

## Decisão

Adotar a opção 3. A interface `CotacaoProvider` mora no **domínio** (`cotacao.dominio`) e define uma única operação: `Cotacao obter(Moeda moeda)`.

Duas implementações em `cotacao.infra`, selecionadas por `@ConditionalOnProperty("cotacao.provedor")`:

- **`CotacaoLocalProvider`** (`local`, padrão via `matchIfMissing = true`) — lê a tabela `cotacoes` do H2. Não precisa de rede. `PUT /cotacoes/{moeda}` altera o valor para simular variação de mercado ao vivo.
- **`CotacaoExternaProvider`** (`externo`) — `RestClient` com timeout de 3s contra a awesomeapi, com **`AwesomeApiAdapter`** traduzindo o JSON externo para o nosso modelo.

O padrão é o provedor que **não precisa de internet**: o piso seguro é sempre o que roda em qualquer lugar.

## Consequências

**Boas**
- `CotacaoService` não sabe de onde vem o número. Trocar a origem é trocar uma linha de YAML — nenhuma recompilação de domínio (OCP e DIP na prática, e é isso que a Aula 7 vai revisitar).
- O formato do fornecedor **não atravessa** a fronteira do domínio: o Adapter é a única classe que conhece `bid` e `create_date`. Trocar de provedor custa uma classe.
- O Adapter é testável **sem rede**: a conversão é função pura sobre um objeto Java. Todos os casos ruins (par ausente, valor podre, data em outro formato) são testados em milissegundos.
- A Aula 5 fica preparada: `CotacaoProvider` é a costura por onde `cotacao` sai do monólito.

**Ruins / a pagar depois**
- Mais classes para o mesmo resultado. Com um provedor só, isso seria abstração prematura — a segunda implementação é o que paga a interface.
- Duas configurações significam **dois comportamentos possíveis em produção**: um bug que só aparece com `externo` não aparece nos testes padrão. Mitigação parcial: `SelecaoDoProvedorExternoTest` prova ao menos que o bean certo é escolhido.
- `PUT /cotacoes/{moeda}` só faz sentido com o provedor local. É uma assimetria assumida, documentada em `docs/contrato-cotacao.md`.

## Alternativa descartada

`@Profile("externo")` em vez de `@ConditionalOnProperty`. Funciona, mas mistura duas ideias: perfil é "em que ambiente estou", propriedade é "que comportamento quero". A origem da cotação é escolha de comportamento — e pode variar dentro do mesmo ambiente.
