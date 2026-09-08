# ADR-005 — Inversão de dependência no `cambio-service` (portas no domínio, Adapters na infra)

- **Status:** aceita
- **Data:** Aula 7
- **Decisores:** time do projeto guiado (turma 1749)
- **Depende de:** ADR-004 (discovery + Feign)

## Contexto

A Aula 6 entregou o comportamento certo com o desenho errado. Duas dívidas ficaram
registradas no `KANBAN.md` no mesmo dia em que a US-08 foi para DONE:

```java
// br.com.ada.cambio.ordem.dominio.OrdemService
import br.com.ada.cambio.ordem.infra.ClienteClient;   // domínio importando infra
```

```java
// br.com.ada.cambio.ordem.api.TratadorDeErros
import feign.FeignException;                          // a borda conhecendo a lib HTTP
```

Três consequências mensuráveis:

1. **Trocar de cliente HTTP mexeria em três camadas.** Feign → `@HttpExchange` (ou gRPC)
   obrigaria a editar `infra`, `dominio` e `api`.
2. **Os testes de domínio precisavam de tipos de biblioteca.** `OrdemServiceTest` construía
   `FeignException` na mão só para simular "cliente não existe". Quando um teste de regra de
   negócio precisa importar a lib de transporte, o desenho está pedindo socorro.
3. **`OrdemService` fazia quatro coisas** — validar, integrar, calcular, persistir — e por
   isso tinha quatro razões para mudar.

## Decisão

**1. As portas pertencem ao domínio.** `ConsultaCliente` e `ConsultaCotacao` são interfaces
em `br.com.ada.cambio.ordem.dominio`, com **modelos de domínio** próprios (`ClienteEncontrado`,
`CotacaoVigente`) — não os DTOs do wire.

**2. As interfaces Feign viram detalhe de transporte.** `ClienteFeignClient` e
`CotacaoFeignClient` ficam em `infra`, devolvendo `*ResumoJson` — o nome termina em `Json`
para deixar claro que é o formato **do outro**.

**3. Adapters ligam os dois lados.** `ClienteClientAdapter implements ConsultaCliente` traduz
**dado** (JSON → modelo de domínio) e **erro** (`FeignException.NotFound` →
`ClienteNaoEncontradoException`, resto → `ServicoIndisponivelException`).

**4. SRP no `OrdemService`.** Validação → `ValidadorDeOrdem`; cálculo →
`CalculadoraDeOperacao`; orquestração e persistência ficam no service.

**5. Injeção por construtor em todo lugar, campos `final`.** Já era o padrão do projeto desde
a Aula 1; ficou registrado o contra-exemplo com `@Autowired` em campo em
`docs/antes-depois-solid.md`, para a turma reconhecer o antipadrão em código legado.

**6. Constantes nomeadas.** `RegrasDeCambio.TAMANHO_CPF`, `TAMANHO_AGENCIA`,
`ESCALA_MONETARIA`, `ESCALA_COTACAO`, `ARREDONDAMENTO` — e `RegrasDeCliente.TAMANHO_CPF` no
cliente-service. Os padrões de regex são expressões constantes em tempo de compilação, então
funcionam dentro de `@Pattern` e `@Column`.

## Por que a inversão vale a pena aqui

O critério não é "SOLID é bom". É este: **o domínio de câmbio vai sobreviver ao Feign.**

O cálculo do valor da operação, a regra dos 4 dígitos da agência e o catálogo de moedas vão
estar em produção quando o Spring Cloud OpenFeign já tiver sido substituído por outra coisa —
como o `RestTemplate` foi antes dele. Colocar a regra de negócio para depender do transporte
é apostar que o transporte dura mais que a regra. A história diz que não.

**O teste de mesa** — dá para rodar ao vivo em aula:

```bash
grep -rl "import feign" aula-06/cambio-service/src/main   # api/TratadorDeErros.java  (a BORDA)
grep -rl "import feign" aula-07/cambio-service/src/main   # so os dois *ClientAdapter (a INFRA)
```

## Consequências

**Ganhamos:**

- **Uma fronteira de verdade.** `feign` só existe em dois arquivos de produção, ambos em `infra`. Trocar de
  cliente HTTP é reescrever esses dois.
- **Testes de domínio sem biblioteca.** `OrdemServiceTest` não importa `feign`; os únicos
  testes que ainda constroem `FeignException` são os dos Adapters — que é onde a tradução mora.
- **Regras puras isoladas.** `ValidadorDeOrdemTest` e `CalculadoraDeOperacaoTest` rodam sem
  Spring, sem mock, em milissegundos.
- **Erro em linguagem de negócio.** O `@RestControllerAdvice` fala de cliente, moeda, agência
  e indisponibilidade — não de códigos HTTP de terceiros.

**Pagamos:**

- **Mais classes.** Duas interfaces, dois records de domínio, dois Adapters e dois records de
  JSON onde antes havia duas interfaces Feign. **Isto é caro e precisa ser dito em aula:**
  a inversão só se paga quando o domínio tem regra própria (o nosso tem) e mais de um
  consumidor potencial da porta. Num CRUD que só repassa dados, seria cerimônia.
- **Um salto a mais para ler o código.** Quem procura "onde a chamada HTTP acontece" passa
  pela porta antes de chegar ao transporte. Mitigado pelos nomes: `*Adapter` e `*FeignClient`
  dizem o que são.
- **Modelos "duplicados"** (`ClienteEncontrado` × `ClienteResumoJson`). É duplicação
  **aparente**: eles mudam por razões diferentes — um quando o nosso domínio muda, outro
  quando o contrato do vizinho muda. Colapsá-los num só acopla as duas mudanças.
- **Os testes unitários mudaram de forma.** Isso não é violação do "refatorar não muda
  comportamento": o comportamento observável — status HTTP e corpo das respostas — é idêntico,
  e os testes de controller provam isso com as **mesmas asserções** da Aula 6. O que mudou
  foram os colaboradores dos testes de unidade, e essa é a natureza de uma refatoração de
  estrutura interna.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| **Manter como estava** | Defensável para um serviço pequeno e estável. Descartada porque o `cambio-service` é o que tem regra de negócio de verdade — e regra de negócio acoplada a lib de transporte é a dívida que mais custa depois. |
| **Feign `ErrorDecoder` em vez de Adapter** | Resolveria a tradução de *erro*, mas não a de *dado*, e o `ErrorDecoder` continua sendo configuração da lib. O Adapter resolve os dois de uma vez, num objeto testável com `new`. |
| **Reaproveitar `ClienteResumoJson` como modelo de domínio** | Menos classes, mas amarra o nosso domínio ao JSON do vizinho: um campo renomeado lá atravessaria até a entidade daqui. |
| **Mockar `ValidadorDeOrdem` e `CalculadoraDeOperacao` no `OrdemServiceTest`** | Seria "mais unitário" e menos útil: esconderia justamente a regra que o teste deveria exercitar. Objeto puro e rápido entra real no teste. |
