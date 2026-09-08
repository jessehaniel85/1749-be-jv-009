# ADR-003 — Quebrar o monólito `cambio-api` em três serviços

- **Status:** aceita
- **Data:** Aula 5
- **Decisores:** time do projeto guiado (turma 1749)
- **Contexto anterior:** ADR-001 (monólito modular), ADR-002 (contrato `CotacaoProvider`)

## Contexto

Ao fim da Aula 4 o `cambio-api` tinha três domínios (`cliente`, `cotacao`, `ordem`) em um
único deployable, com uma única base H2. Isso funcionava. Três pressões apareceram:

1. **Ritmo de mudança diferente.** A cotação muda de fonte (local ↔ externo) e de valor o
   tempo todo; o cadastro de cliente é estável há semanas. Subir tudo junto para mexer em
   cotação é caro e arriscado.
2. **Perfil de carga diferente.** Consulta de cotação é ordens de grandeza mais frequente
   que cadastro. Escalar o monólito escala o cadastro à toa.
3. **Fronteira de time.** O cadastro de cliente é candidato natural a virar serviço
   corporativo compartilhado; a ordem de compra é do produto de câmbio.

## Por que **agora** e não antes

Porque agora existe a única coisa que torna a quebra segura: **a suíte de testes da Aula 2/3**.
Quebrar sem rede de proteção é reescrever às cegas.

E porque as fronteiras já estavam **desenhadas e estáveis**: os pacotes `cliente`, `cotacao`
e `ordem` da Aula 3 sobreviveram a duas aulas sem que ninguém precisasse mover classe de
lugar. Fronteira que não se move há tempo é fronteira madura — esse é o sinal.

**Não fizemos microsserviço primeiro de propósito.** O monólito modular das Aulas 1–4 é o que
permitiu descobrir onde ficam as costuras. Quem começa distribuído descobre as costuras
depois de já ter pagado o custo da rede.

## Por onde cortar

Cortamos **pelo dono do dado**, não pela camada. Nunca por `controller-service` / `repo-service`.

| Serviço | Dado que ele **possui** | Regra que ele **decide** |
|---|---|---|
| `cliente-service` | tabela `clientes` | CPF é único; CPF inexistente é 404 |
| `cotacao-service` | tabela `cotacoes` | qual fonte vale hoje; moeda fora do catálogo é 422 |
| `cambio-service` | tabela `ordens_de_compra` | como calcular o total; agência tem 4 dígitos |

O teste de que o corte está certo: **cada regra da coluna da direita tem exatamente um dono**.
Nenhuma delas precisa de duas bases para ser decidida.

## Dados como fronteira

Esta é a decisão que dá a quebra de verdade — o resto é encanamento.

- **Três bases H2 separadas.** Nenhum serviço lê a tabela do outro. Se `cambio-service`
  pudesse dar `SELECT` em `clientes`, teríamos três deployables e **um** sistema acoplado:
  o pior dos dois mundos (a "monólito distribuído").
- **Sem chave estrangeira entre bases.** `OrdemDeCompra` guarda `idCliente` e `cpfCliente`
  como **cópia do que o cliente-service respondeu naquele instante**. O comprovante é um
  documento histórico: se o cliente mudar de nome amanhã, a ordem de ontem não muda.
- **Enum `Moeda` duplicado** em `cotacao-service` e `cambio-service`. Deliberado. Extrair um
  jar `cambio-comum` criaria acoplamento de build entre serviços que queremos independentes:
  toda mudança no jar viraria release coordenado dos três. O que une os serviços é o
  **contrato HTTP**, não uma classe compartilhada. Duplicar três linhas de enum é mais
  barato que sincronizar releases.

## Consequências

**Ganhamos:** deploy, escala e evolução independentes; falha isolada (cotação fora do ar não
derruba o cadastro); fronteira de time explícita.

**Pagamos:**

- **Rede como modo de falha novo.** Nasceu o `503` no `TratadorDeErros` e o timeout de 3s em
  `ClientesHttpConfig`. Chamada local que não falhava agora falha.
- **Consistência eventual.** Não há transação atravessando os três serviços. Aceitamos porque
  a gravação da ordem é o **último** passo do fluxo — se ela falhar, nada ficou pela metade.
- **Custo operacional.** Três processos, três portas, três bases. Em aula: três terminais.
- **URL na mão.** `servicos.cliente.url` está escrito no `application.yml`. É dívida técnica
  assumida e datada: some na Aula 6 (ADR-004).

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Continuar monólito modular | Legítima, e seria a escolha certa num time de 3 pessoas. Descartada por **objetivo didático** do módulo — e porque as três pressões acima são reais no cenário do projeto. |
| Quebrar em 2 (cliente + resto) | Deixaria cotação e ordem grudadas justamente onde os ritmos de mudança mais divergem. |
| Uma base compartilhada pelos 3 | O acoplamento pior: schema vira contrato implícito, e migração de tabela vira reunião de três times. |
| Jar comum `cambio-dominio` | Acopla o build. Volta a ser um monólito com passos extras de deploy. |
