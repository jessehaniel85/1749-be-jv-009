# Sessão 1 — Kickoff: contexto, problema e escopo

- **Projeto:** API de Câmbio — compra de moeda estrangeira com retirada em agência
- **Data:** 05/08/2026 (quarta-feira), 10h00–10h35
- **Plataforma:** videoconferência
- **Participantes:** Renata Vasconcelos (PO, facilitadora), Cláudio Menezes (Gerente de agência), Wagner Duarte (Atendimento e Canais), Iuri Sampaio (Tech lead)
- **Ausentes:** Marcos Yamaguti (Tesouraria — entra na Sessão 3) e Dra. Alice Nogueira (Compliance — entra na Sessão 2). Marcos pediu que **nada de formação de preço** fosse fechado sem ele.

---

**Renata:** Bom dia, pessoal. O Marcos e a Alice não vêm hoje — o Marcos mandou mensagem pedindo pra gente não fechar nada de cotação, spread, preço, sem ele. Anotei. Reservei a Sessão 3 inteira pra isso.

**Cláudio:** Ele tem razão. Preço com a mesa é briga feia.

**Renata:** É. Hoje é kickoff: sair daqui com o problema claro e o escopo cravado — o que entra no MVP e, principalmente, o que **não** entra. Vou anotando um glossário, porque cada área chama a mesma coisa de um nome. Wagner, começa você, que recebe a reclamação.

**Wagner:** Então. O fluxo de compra de moeda estrangeira hoje é… telefone. O cliente vai viajar, quer dois mil dólares, liga na central. A agência liga na mesa pra saber a cotação, anota num papel, o cliente diz que vai pensar, e quando liga de volta a cotação já é outra.

**Cláudio:** E aí ele reclama comigo. "Mas ontem você me falou cinco e quarenta." Falei mesmo. Ontem.

**Wagner:** Exato. E tem o cliente que já decidiu e quer registrar o pedido às onze da noite, do sofá. Hoje ele espera abrir agência.

**Renata:** Deixa eu anotar o problema central: **o cliente não consegue, sozinho e pelo app, consultar a cotação e registrar um pedido de compra de moeda estrangeira.** Depende de telefone e de horário de agência.

**Cláudio:** E na ponta tem outro pedaço: muito cliente chega aqui querendo dólar sem pedido registrado. Eu não tenho a moeda separada, ele volta outro dia. Péssimo.

**Iuri:** Isso muda o escopo. Cláudio, quando o cliente registra o pedido no app, **sai dinheiro da conta dele naquele momento**?

**Cláudio:** Não sai. Ele paga no caixa, na hora que retira. O pedido é… uma reserva, um aviso: "separa dois mil dólares pra mim na agência tal".

**Iuri:** Era o que eu precisava ouvir. Então anota em negrito: **este sistema não movimenta dinheiro**. Não debita conta, não faz pagamento, não integra com o core de conta corrente. Ele registra uma **ordem de compra**; o pagamento acontece no caixa, fora do nosso sistema.

**Renata:** Anotado em negrito. **Fora de escopo: pagamento, débito em conta, qualquer movimentação financeira.**

**Wagner:** Antes de fechar: entrega em domicílio. Tem concorrente que leva a moeda na casa do cliente, e a diretoria já perguntou isso duas vezes.

**Renata:** Vai aparecer mesmo, mas é fora. Envolve transportadora de valores, seguro, contrato — uma operação que a gente não tem. **Retirada é em agência, ponto.** Fase 2, se um dia for.

**Renata:** Alto nível, então: o que o sistema faz?

**Iuri:** Três capacidades. Uma: **cadastrar o cliente** que vai operar, e consultar esse cadastro depois. Duas: **consultar a cotação** de uma moeda. Três: **registrar a ordem de compra** — quem, qual moeda, quanto, em que agência retira — e devolver um comprovante. Falta alguma coisa?

**Wagner:** É isso. Ah, e ele vai querer ver o pedido de novo depois. "Qual era mesmo o número?"

**Iuri:** Então consultar a ordem também, pelo identificador dela. É a mesma história.

**Renata:** Anotado. Cadastro, cotação, ordem — e a consulta de cada um. Agora, moeda. Quais?

**Cláudio:** Dólar e euro. Noventa e tanto por cento é dólar. Euro tem, bem menos.

**Wagner:** E libra? A gente recebe pedido de libra. Não é muito, mas recebe.

**Cláudio:** Recebe, mas eu não tenho libra em caixa. Tenho que pedir com antecedência, vem da custódia, demora. Se abrir libra no app hoje, o cliente pede e eu não entrego.

**Renata:** Então fica assim: **MVP com dólar e euro.** Libra é fase 2, quando a operação da agência estiver preparada. Wagner, tudo bem?

**Wagner:** Tudo, desde que a mensagem seja boa. Se ele pedir libra e o app der "erro", ele liga pra mim.

**Iuri:** Esse é um ponto de desenho. Se o sistema **só** trabalha com duas moedas, ele precisa recusar as outras de forma explícita e previsível. Nem erro genérico, nem fingir que aceitou. É regra, não detalhe de tela — fecho o formato na próxima sessão.

**Renata:** Pauta da Sessão 2. Mais alguma coisa fora de escopo antes de eu fechar?

**Cláudio:** Venda. O cliente que **volta** da viagem com dólar sobrando e quer vender pra gente. Outro fluxo, outro preço, outra regra.

**Renata:** Fora. **O MVP é só compra.**

**Iuri:** E eu queria pedir uma coisa antes de terminar, Renata. Espaço no backlog pra história técnica.

**Renata:** Fala.

**Iuri:** O sistema começa pequeno, e a tentação vai ser fazer tudo num projeto só e nunca mais mexer. Vou precisar de cards no board pra coisas que **não** são funcionalidade que o cliente vê: teste automatizado, contrato de serviço pra cotação, separar o sistema em serviços quando ele crescer, refatoração. Se não estiver no board, não acontece — vira "quando der tempo", e nunca dá.

**Renata:** Eu chamo isso de história habilitadora. Entram no mesmo backlog, com critério de aceite igual às outras. Mas eu vou te cobrar o porquê de cada uma. Não aceito "porque é boa prática".

**Iuri:** Justo. Eu trago o porquê.

**Wagner:** Uma dúvida de volume: quantas ordens por dia? Se for igual ao PIX…

**Iuri:** Não é. Câmbio de varejo é volume baixo. O desafio não é aguentar pico, é **estar correto** — o valor tem que bater no centavo — e **conseguir mudar** sem quebrar.

**Renata:** Recapitulando: o sistema **cadastra cliente, consulta cotação e registra ordem de compra com retirada em agência**. Não movimenta dinheiro, não entrega em domicílio, não compra do cliente. Dólar e euro; libra é fase 2. Semana que vem: cadastro, ordem, erros e mensagens, com a Alice. Sessão 3, cotação, com o Marcos.

**Cláudio:** Fechado. Só me garante que o número da agência vai chegar certo, que aí eu fico feliz.

**Renata:** Anotei até isso. Até semana que vem.

---

## Decisões da Sessão 1

1. O sistema **cadastra clientes, consulta cotações e registra ordens de compra** de moeda estrangeira, com **retirada em agência**. Consultar cliente e consultar ordem entram junto.
2. O sistema **não movimenta dinheiro**: sem débito em conta, sem pagamento, sem integração com core de conta corrente. O pagamento ocorre no caixa, na retirada.
3. **Fora de escopo:** pagamento pelo app, entrega em domicílio e venda de moeda pelo cliente (só compra).
4. **Moedas do MVP: dólar (USD) e euro (EUR).** Libra é **fase 2**, condicionada ao preparo da operação nas agências.
5. Moeda fora do catálogo deve ser **recusada de forma explícita e previsível** — formato a definir na Sessão 2.
6. **Histórias técnicas (enabler) entram no mesmo backlog**, com critério de aceite e justificativa de negócio. Nada de cotação/spread sem a Tesouraria.

## Action items

- **Renata:** consolidar o glossário, montar o board e garantir a presença da Dra. Alice na Sessão 2 e do Marcos na Sessão 3.
- **Iuri:** trazer para a Sessão 2 os formatos de erro e a diferença entre "requisição malformada" e "requisição válida que não pode ser processada".
- **Wagner:** trazer os textos de mensagem que o cliente vê hoje na central.

## Glossário inicial (linguagem ubíqua)

- **Cliente:** pessoa cadastrada para operar câmbio no app.
- **Cotação:** quanto vale uma unidade da moeda estrangeira em reais, num dado momento.
- **Ordem de compra:** o pedido registrado pelo cliente — moeda, quantidade e agência de retirada.
- **Retirada:** momento em que o cliente vai à agência, paga e recebe as cédulas — **fora** deste sistema.
- **Fase 2:** catalogado, não construído agora (libra, domicílio, venda).
