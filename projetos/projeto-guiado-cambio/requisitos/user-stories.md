# User Stories — API de Câmbio

> **Documento compilado por:** Renata Vasconcelos (Product Owner, Produto de Câmbio)
> **Data:** 20/08/2026
> **Base:** três sessões de elicitação (kickoff, cadastro e ordem, cotação e integrações)
> **Status:** primeira fonte de verdade para o time de desenvolvimento.

Este documento é o ponto de partida do desenvolvimento e a origem dos cards do board. As transcrições das três reuniões estão no mesmo diretório e devem ser consultadas para entender o contexto e dirimir dúvidas. Compilei na noite da última sessão para não segurar o time; **em caso de divergência, recorram às transcrições** — vocês falaram, está gravado.

Histórias marcadas como **enabler** são histórias técnicas: não entregam funcionalidade visível ao cliente, mas habilitam as demais. Foram acordadas com o time e entram no mesmo backlog, com o mesmo rigor de critério de aceite.

## Glossário (linguagem ubíqua)

- **Cliente:** pessoa cadastrada para operar câmbio. Identificada por CPF.
- **Moeda:** moeda estrangeira do catálogo — **USD, EUR ou GBP**.
- **Cotação:** quanto vale uma unidade da moeda estrangeira em reais, num dado momento, com a data/hora em que foi apurada.
- **Ordem de compra:** pedido registrado pelo cliente — moeda, quantidade e agência de retirada. Gera um comprovante.
- **Retirada:** o cliente vai à agência, paga e recebe as cédulas. Acontece **fora** deste sistema.
- **Fonte de cotação:** de onde o sistema obtém o preço. Padrão: tabela interna alimentada pela Tesouraria.

---

## Épico 1 — Cliente

### US-01 — Cadastrar um cliente para operar câmbio

**Como** cliente do banco,
**quero** me cadastrar para operar câmbio,
**para** poder registrar ordens de compra de moeda estrangeira pelo app.

**Prioridade:** Alta · **Sprint 1**

**Campos do cliente**

| Campo | Obrigatório | Formato |
|---|---|---|
| nome | Sim | texto |
| cpf | Sim | 11 dígitos, somente números, sem máscara |
| dataNascimento | Sim | data |
| estadoCivil | Sim | texto |
| sexo | Sim | texto |

**Critérios de aceite**

- **Dado** um cadastro com todos os campos obrigatórios válidos,
  **Quando** o cadastro é enviado,
  **Então** o sistema cria o cliente e responde **`201 Created`** com o identificador gerado e os dados cadastrados.

- **Dado** um cadastro com campo obrigatório ausente ou em formato inválido (CPF com máscara, com mais ou menos de 11 dígitos, data inválida),
  **Quando** o cadastro é enviado,
  **Então** o sistema responde **`400`** indicando o campo com problema.

- **Dado** um CPF que já existe na base,
  **Quando** um novo cadastro é enviado com esse mesmo CPF,
  **Então** o sistema cria o novo cadastro e devolve um novo identificador — o CPF pode se repetir, quem identifica o cliente é o identificador gerado.

### US-02 — Consultar um cliente pelo CPF

**Como** sistema (e como atendimento),
**quero** consultar um cliente pelo CPF,
**para** confirmar que ele está apto a registrar uma ordem.

**Prioridade:** Alta · **Sprint 1**

**Critérios de aceite**

- **Dado** um CPF de cliente cadastrado,
  **Quando** a consulta é feita,
  **Então** o sistema responde **`200`** com os dados do cliente.

- **Dado** um CPF que não corresponde a nenhum cadastro,
  **Quando** a consulta é feita,
  **Então** o sistema responde **`404`**, com mensagem compreensível pelo cliente ("não localizamos seu cadastro").

### US-03 — Sustentar a evolução com testes automatizados *(enabler)*

**Como** time de desenvolvimento,
**quero** cobrir o sistema com testes automatizados em três níveis,
**para** conseguir refatorar e evoluir a API sem quebrar comportamento já entregue.

**Prioridade:** Alta · **Sprint 1**

**Critérios de aceite**

- **Dado** um serviço de domínio com regra de negócio,
  **Quando** a suíte é executada,
  **Então** existe **teste unitário** da regra, isolado de banco e de rede.

- **Dado** um endpoint da API,
  **Quando** a suíte é executada,
  **Então** existe **teste do contato pela API** verificando status, corpo e validação de entrada.

- **Dado** o sistema completo,
  **Quando** a suíte é executada,
  **Então** existe ao menos um **teste de integração** cobrindo o fluxo ponta a ponta, e a suíte inteira roda verde em um único comando.

---

## Épico 2 — Cotação

### US-04 — Consultar a cotação de uma moeda

**Como** cliente,
**quero** consultar a cotação atual de uma moeda,
**para** decidir se registro a ordem agora.

**Prioridade:** Alta · **Sprint 1**

**Critérios de aceite**

- **Dado** uma moeda do catálogo (**USD, EUR ou GBP**),
  **Quando** a cotação é consultada,
  **Então** o sistema responde **`200`** com a moeda, o valor da cotação em reais e a data/hora da cotação.

- **Dado** uma sigla de moeda fora do catálogo (por exemplo `JPY`),
  **Quando** a cotação é consultada,
  **Então** o sistema recusa a requisição com **`400`** e a mensagem "no momento trabalhamos com as moedas do catálogo".

- **Dado** que a Tesouraria precisa manter o preço atualizado,
  **Quando** a mesa envia uma nova cotação para uma moeda,
  **Então** o sistema atualiza o valor vigente e registra quem alterou, o valor anterior, o novo e o momento da alteração.

### US-05 — Registrar uma ordem de compra

**Como** cliente cadastrado,
**quero** registrar uma ordem de compra de moeda estrangeira para retirar em uma agência,
**para** garantir que a moeda estará separada quando eu chegar lá.

**Prioridade:** Alta · **Sprint 1**

**Campos da ordem**

| Campo | Obrigatório | Formato |
|---|---|---|
| cpfCliente | Sim | 11 dígitos |
| moeda | Sim | moeda do catálogo |
| valorMoedaEstrangeira | Sim | valor positivo, na moeda estrangeira |
| numeroAgenciaRetirada | Sim | 3 dígitos |

**Notas de domínio**
- O sistema **não movimenta dinheiro**: o pagamento acontece no caixa da agência, na retirada.
- O **total em reais é calculado pelo sistema**, nunca informado pelo cliente.
- A operação deve respeitar os limites definidos pelo compliance.

**Critérios de aceite**

- **Dado** um CPF de cliente cadastrado, uma moeda do catálogo, um valor positivo e uma agência válida,
  **Quando** a ordem é registrada,
  **Então** o sistema responde **`201 Created`** com o comprovante: identificador da ordem, cliente, data/hora da solicitação, moeda, valor em moeda estrangeira, cotação utilizada, valor total da operação e agência de retirada.

- **Dado** uma ordem válida,
  **Quando** o total da operação é calculado,
  **Então** o valor total é o **valor em moeda estrangeira multiplicado pela cotação vigente**, arredondado **para cima** com **2 casas decimais**.

- **Dado** um CPF que não corresponde a nenhum cliente cadastrado,
  **Quando** a ordem é registrada,
  **Então** o sistema responde **`404`**.

- **Dado** uma moeda fora do catálogo ou uma agência de retirada inexistente,
  **Quando** a ordem é registrada,
  **Então** o sistema responde **`422`** com mensagem explicando o motivo da recusa.

- **Dado** uma ordem registrada,
  **Quando** o cliente consulta a ordem pelo identificador,
  **Então** o sistema responde **`200`** com o mesmo comprovante; se o identificador não existir, **`404`**.

### US-06 — Isolar a origem da cotação atrás de um contrato *(enabler)*

**Como** time de desenvolvimento,
**quero** que a origem da cotação fique atrás de um contrato único,
**para** poder trocar a fonte de preço sem alterar o resto do sistema.

**Prioridade:** Média · **Sprint 2**

**Critérios de aceite**

- **Dado** o sistema em execução padrão,
  **Quando** uma cotação é obtida,
  **Então** ela vem da **tabela interna alimentada pela Tesouraria** — essa é a fonte padrão em produção.

- **Dado** um ambiente fora da rede corporativa,
  **Quando** a fonte externa é habilitada **por configuração**,
  **Então** o sistema passa a obter a cotação do provedor externo **sem que nenhum outro componente mude**, adaptando o formato externo para o modelo interno.

- **Dado** o contrato da fonte de cotação,
  **Quando** uma nova implementação é adicionada,
  **Então** ela é plugada sem alterar quem consome a cotação, e a escolha é feita por configuração, não por código condicional espalhado.

---

## Épico 3 — Evolução da arquitetura

### US-07 — Separar o sistema em serviços por domínio *(enabler)*

**Como** time de desenvolvimento,
**quero** separar o sistema em serviços de cliente, cotação e câmbio,
**para** que cada domínio evolua, seja testado e seja implantado de forma independente.

**Prioridade:** Média · **Sprint 2**

**Critérios de aceite**

- **Dado** o sistema separado,
  **Quando** os serviços sobem,
  **Então** existem três serviços — **cliente**, **cotação** e **câmbio (dono da ordem)** —, cada um com **sua própria base de dados**, sem acesso direto à base do outro.

- **Dado** o serviço de câmbio,
  **Quando** ele precisa de dados de cliente ou de cotação,
  **Então** ele os obtém **pelo contrato exposto** por cada serviço, nunca por consulta direta a tabela alheia.

- **Dado** a separação,
  **Quando** a suíte é executada,
  **Então** cada serviço tem seus próprios testes e o fluxo de ponta a ponta continua funcionando.

### US-08 — Localizar e chamar os serviços sem endereço fixo *(enabler)*

**Como** time de desenvolvimento,
**quero** que os serviços se localizem por nome, e não por URL escrita em arquivo,
**para** que mudança de ambiente ou de instância não vire incidente.

**Prioridade:** Média · **Sprint 2**

**Critérios de aceite**

- **Dado** os serviços em execução,
  **Quando** um deles sobe,
  **Então** ele se registra em um **mecanismo de localização** e passa a ser encontrável **pelo nome lógico**.

- **Dado** o serviço de câmbio,
  **Quando** ele chama cliente ou cotação,
  **Então** a chamada é feita por um **cliente declarativo** que referencia o **nome do serviço**, sem endereço fixo no código.

- **Dado** uma instância indisponível,
  **Quando** a chamada falha,
  **Então** o erro é tratado e devolvido de forma compreensível, sem vazar detalhe de infraestrutura para o cliente.

---

## Épico 4 — Qualidade interna

### US-09 — Refatorar o código sem alterar comportamento *(enabler)*

**Como** time de desenvolvimento,
**quero** refatorar o sistema aplicando Clean Code e SOLID,
**para** que o código continue barato de mudar à medida que as regras crescerem.

**Prioridade:** Média · **Sprint 3**

**Critérios de aceite**

- **Dado** o código refatorado,
  **Quando** a suíte de testes é executada,
  **Então** ela permanece **verde sem alteração dos testes** — a refatoração não muda comportamento observável.

- **Dado** os serviços,
  **Quando** o código é revisado,
  **Então** as dependências são **explícitas no construtor**, não há número mágico solto (tamanhos de CPF e de agência, escala do arredondamento são constantes nomeadas) e cada classe tem uma responsabilidade identificável em uma frase.

- **Dado** o serviço de ordem,
  **Quando** o fluxo é lido,
  **Então** validação, cálculo e persistência estão separados, e o serviço depende de **abstrações**, não de implementações concretas de infraestrutura.

### US-10 — Aplicar padrões de projeto onde há problema real *(enabler)*

**Como** time de desenvolvimento,
**quero** aplicar padrões de projeto nos pontos que já sabemos que vão variar,
**para** absorver regra nova sem cirurgia no código existente.

**Prioridade:** Baixa · **Sprint 3**

**Critérios de aceite**

- **Dado** que o spread e o arredondamento tendem a divergir entre moedas,
  **Quando** o total da operação é calculado,
  **Então** o cálculo está **isolado por moeda**, de modo que uma regra nova entre como peça nova, sem alterar o cálculo das demais.

- **Dado** a orquestração cliente → cotação → ordem,
  **Quando** o controlador registra uma ordem,
  **Então** ele conversa com **um único ponto de entrada** que esconde a orquestração, em vez de coordenar os três serviços diretamente.

- **Dado** cada padrão aplicado,
  **Quando** a decisão é tomada,
  **Então** existe um **registro curto de decisão (ADR)** dizendo qual problema o padrão resolve e qual alternativa foi descartada. Padrão sem problema real não entra.

---

## Requisitos transversais (compliance e auditoria)

- Os dados do cliente são **dado pessoal** e o tratamento se dá sob obrigação legal (ficha cadastral de câmbio). Não expor dado do cliente em log nem em mensagem de erro.
- **Trilha de auditoria:** registrar quem registrou cada ordem e quem consultou cada cadastro.
- As ordens devem ser **guardadas pelo prazo legal**. No MVP, nada é apagado.

## Fora do escopo do MVP (catálogo / fase 2)

- **Pagamento e qualquer movimentação financeira** — o pagamento ocorre no caixa da agência, fora deste sistema. Fora do escopo, sempre.
- **Entrega em domicílio** — retirada é em agência.
- **Venda de moeda pelo cliente** — o MVP é só compra.
- **Trava de preço** (garantir a cotação por um período) — exige operação de hedge.
- **Validação de limite operacional por cliente** — regra de compliance, fase 2.
- **Rotina de descarte** após o prazo de retenção.
