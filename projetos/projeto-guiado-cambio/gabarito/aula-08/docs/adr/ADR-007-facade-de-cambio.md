# ADR-007 — `CambioFacade` como porta de entrada da operação de câmbio

- **Status:** aceita
- **Data:** Aula 8
- **Decisores:** time do projeto guiado (turma 1749)
- **Depende de:** ADR-005 (SRP e DIP), ADR-006 (Strategy)

## Contexto

Depois da Aula 7 e do Strategy da Aula 8, registrar uma ordem passou a envolver **cinco**
colaboradores, numa sequência que importa:

1. `ValidadorDeOrdem` — resolve a moeda e confere a agência;
2. `ConsultaCliente` — busca o cliente no `cliente-service`;
3. `ConsultaCotacao` — busca a cotação no `cotacao-service`;
4. `CalculadoraDeOperacao` → estratégia da moeda — calcula o total;
5. `OrdemService` — persiste.

A ordem não é decorativa: **validar antes** economiza duas chamadas de rede em toda requisição
malformada, e consultar o cliente antes da cotação economiza uma quando o CPF não existe.

Duas saídas ruins estavam na mesa:

- **o controller conhece os cinco** — a camada de borda passa a carregar regra de sequência,
  e todo novo ponto de entrada (uma fila, um job noturno, um teste de aceitação) repete a
  coreografia. Coreografia duplicada sai de sincronia;
- **o `OrdemService` continua fazendo tudo** — volta a ter duas razões para mudar
  (orquestrar e persistir), desfazendo o SRP da Aula 7.

## Decisão

`CambioFacade` (`@Service`, pacote `dominio`) assume a orquestração e expõe dois métodos:
`registrar(NovaOrdem)` e `consultarPorId(Long)`.

`OrdemController` passa a depender **só** dela. `OrdemService` encolhe para persistência
(`salvar`, `buscarPorId`).

**A Facade não contém regra própria** — nenhuma validação, nenhum cálculo. Se um dia aparecer
um `if` de negócio dentro dela, o padrão degenerou em *God Object* e é hora de extrair.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| **Controller orquestrando** | Espalha a sequência pela camada mais volátil do sistema, e cada novo canal de entrada a duplica. |
| **`OrdemService` continuar orquestrando + persistindo** | Funciona (era a Aula 7), mas devolve à classe duas razões para mudar. A Facade separa "o passo a passo" de "o acesso ao dado". |
| **Application Service / caso de uso por classe** (`RegistrarOrdemUseCase`) | Desenho legítimo e mais granular — é para onde iríamos com dez casos de uso. Com **dois**, uma classe por caso multiplicaria arquivos sem separar nada de verdade. |
| **Orquestrador genérico / pipeline configurável** | Abstração especulativa clássica: infraestrutura para variação que ninguém pediu. |

## Consequências

**Ganhamos:**

- O controller tem **uma** dependência. O teste dele mocka **um** bean — visível no
  `OrdemControllerTest`, que encolheu.
- A coreografia tem um lugar só, com teste próprio. `CambioFacadeTest` verifica inclusive a
  **ordem** — com `verifyNoInteractions(consultaCliente, consultaCotacao)` quando a validação
  falha. Teste de sequência é o tipo de coisa que se quebra silenciosamente sem ele.
- Um segundo canal de entrada (fila, job, CLI) reaproveita a Facade inteira.

**Pagamos:**

- **Mais um salto na leitura.** Quem investiga um bug passa por controller → facade →
  colaborador. Aceitável: a facade cabe numa tela e não esconde regra.
- **Risco de virar God Object.** É o risco real do padrão. Mitigação declarada: a Facade não
  ganha `if` de negócio; regra nova vai para um colaborador. Vale revisar isso em code review.
- **Cinco parâmetros no construtor.** Feio de propósito — é o alarme do SRP funcionando. Se
  virar oito, é sinal de que existe um caso de uso escondido pedindo para nascer.
