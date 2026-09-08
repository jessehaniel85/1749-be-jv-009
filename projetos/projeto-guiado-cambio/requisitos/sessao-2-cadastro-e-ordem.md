# Sessão 2 — Cadastro do cliente e ordem de compra

- **Projeto:** API de Câmbio — compra de moeda estrangeira com retirada em agência
- **Data:** 12/08/2026 (quarta-feira), 10h00–10h40
- **Plataforma:** videoconferência
- **Participantes:** Renata Vasconcelos (PO, facilitadora), Cláudio Menezes (Gerente de agência), Wagner Duarte (Atendimento e Canais), Dra. Alice Nogueira (Compliance/PLD), Iuri Sampaio (Tech lead)
- **Ausente:** Marcos Yamaguti (Tesouraria — entra na Sessão 3)

---

**Renata:** Oi, pessoal. Alice, bem-vinda. Pauta de hoje: quais dados guardamos do cliente, o que vai na ordem, e o que acontece quando dá errado. Cotação fica pra semana que vem, com o Marcos. Alice, ficha cadastral: o que é obrigatório?

**Alice:** Para câmbio, mesmo de varejo, eu preciso da pessoa identificada. Nome completo, CPF, data de nascimento, estado civil e sexo. É o mínimo da ficha e é o que a auditoria procura.

**Wagner:** Estado civil? O cliente vai perguntar por que a gente quer saber isso pra ele comprar duzentos dólares.

**Alice:** Vai mesmo. A resposta é: consta da ficha cadastral de câmbio. Mas concordo que precisa estar explicado na tela.

**Renata:** Cinco campos obrigatórios, então. Iuri, do lado técnico, algum problema?

**Iuri:** Nenhum, mas preciso fechar o CPF, que é o campo que sempre dá confusão. Chega com máscara ou só o número?

**Wagner:** No app o cliente digita com máscara. Mas a máscara é da tela.

**Iuri:** Então guardamos **só os onze dígitos**, sem ponto nem traço, e como texto — CPF que começa com zero existe, e número inteiro come o zero da frente.

**Renata:** Onze dígitos, só número, sem máscara. Anotado.

**Iuri:** E a pergunta mais importante: **CPF pode repetir?** Posso ter dois cadastros com o mesmo CPF?

**Cláudio:** Não. Uma pessoa, um cadastro. Se eu tiver dois, quando o cliente chegar no balcão eu não sei qual é.

**Alice:** E é mais forte que isso: para PLD, o CPF **é** a identidade do cliente. Com duplicidade eu não consigo somar as operações da mesma pessoa.

**Iuri:** Fecha, e resolve outra coisa: a consulta do cliente vai ser **pelo CPF**. Se existissem dois cadastros com o mesmo CPF, qual eu devolvo? Então: **CPF é único no sistema.** Segundo cadastro com o mesmo CPF é recusado, com mensagem clara de que já existe.

**Wagner:** "CPF já cadastrado", e um caminho. Não pode ser "erro ao salvar".

**Renata:** E a consulta por CPF: se não achar?

**Iuri:** **Não encontrado** — o 404. E não encontrado é resposta legítima, não é falha do sistema.

**Renata:** Agora a ordem. Cláudio, o que precisa estar no pedido pra ele ser útil na sua agência?

**Cláudio:** Quem é o cliente, qual moeda, quanto ele quer, e **em qual agência ele retira**. Sem a agência eu não separo cédula.

**Renata:** O "quanto" é em real ou em dólar?

**Cláudio:** Em dólar. O cliente fala "quero dois mil dólares", não "dez mil reais em dólar".

**Iuri:** Então o cliente informa o **valor na moeda estrangeira** e o sistema calcula o valor em reais aplicando a cotação. O total é resultado, não entrada — ninguém manda esse número pra gente.

**Alice:** Isso é bom pra mim. Se o cliente mandasse o total, o comprovante já nasceria inconsistente.

**Renata:** Fechado. E a agência — que formato?

**Cláudio:** Toda agência nossa tem **quatro dígitos**. A minha é 0578.

**Iuri:** Com o zero na frente?

**Cláudio:** Com o zero. E é aí que dá problema hoje: chega papel com "578", o caixa procura e não acha. **Quatro dígitos**, sempre, com o zero.

**Iuri:** Então mesma regra do CPF: **texto, quatro caracteres, só dígito**. Se vier com três, ou com cinco, a gente recusa — não adivinha, não completa com zero.

**Renata:** Anotei: **agência de retirada, quatro dígitos, obrigatório**. E o que o cliente recebe de volta?

**Cláudio:** Um comprovante, com um número que ele possa me dizer no balcão.

**Iuri:** A resposta traz: identificador da ordem, cliente, data e hora da solicitação, moeda, valor em moeda estrangeira, **a cotação usada**, o total em reais e a agência. Como é criação de recurso novo, é um **201**, "criado".

**Renata:** Por que a cotação no comprovante? Não é redundante?

**Alice:** Não. Se o cliente questionar o valor amanhã, eu preciso saber por qual cotação aquela ordem foi fechada. Sem isso eu não defendo a operação.

**Renata:** Anotado, é auditoria. Agora, erros. Wagner.

**Wagner:** Três casos que já sei que vão acontecer. Um: o cliente pede uma moeda que a gente não tem — libra, iene, peso. Dois: ordem com CPF não cadastrado. Três: o cara erra a agência.

**Iuri:** Aqui eu preciso separar duas coisas que parecem iguais. Uma requisição pode estar **malformada** — falta campo obrigatório, data sem sentido, valor negativo. Isso é "não entendi o que você mandou": **400**.

**Wagner:** Tá. E a libra?

**Iuri:** A libra é diferente. A requisição está perfeita, eu entendi tudo — "GBP" é moeda de verdade, campo preenchido, formato certo. Só que eu **não posso processar**, porque não está no nosso catálogo. Não é "não entendi", é "entendi e não posso aceitar". Para esse caso existe código próprio: **422**.

**Renata:** Deixa eu repetir, senão eu anoto errado: campo faltando ou formato quebrado é **400**; moeda válida fora do catálogo é **422**.

**Iuri:** Isso. E vale pra agência: "578", três dígitos, é formato — 400. Quatro dígitos de agência inexistente é 422.

**Wagner:** E CPF não cadastrado?

**Iuri:** **404**. O cliente que você referenciou não existe.

**Wagner:** Beleza. Só me deixa escrever os textos. "Ocorreu um erro" não serve. Tem que ser "no momento trabalhamos com dólar e euro" e "não localizamos seu cadastro".

**Renata:** Você escreve, eu ponho na história. Alice, faltou o seu tema: limites.

**Alice:** Faltou, e é importante. Existe limite operacional por pessoa: hoje, em espécie, o equivalente a **dez mil dólares por CPF por dia**. Acima disso não é proibido, mas passa por análise e vira reporte.

**Renata:** Entra no MVP?

**Alice:** O ideal seria sim.

**Iuri:** É maior do que parece. Somar o que o CPF comprou no dia significa consultar o histórico, e o limite é em dólar mas a ordem pode ser em euro — tem conversão no meio. É uma regra inteira.

**Renata:** Então registro em ata: **o limite é fase 2**, não entra no MVP. Alice, aceita, com a condição de que o desenho não impeça colocar depois?

**Alice:** Aceito, com a condição em ata. E quero o número registrado mesmo sem implementar: dez mil dólares por CPF por dia.

**Iuri:** E eu deixo o ponto de extensão: a validação da ordem fica isolada, pra caber regra nova sem reescrever tudo.

**Renata:** Recapitulando: cinco campos, CPF único de onze dígitos; ordem com cliente, moeda, valor na moeda estrangeira e agência de quatro dígitos; comprovante com a cotação usada; 400 pra formato, 422 pra moeda fora do catálogo, 404 pra quem não existe; limite é fase 2. Semana que vem, cotação.

---

## Decisões da Sessão 2

1. **Cadastro do cliente:** nome, CPF, data de nascimento, estado civil e sexo — todos obrigatórios.
2. **CPF:** texto de **11 dígitos, só números, sem máscara**, e **único no sistema**. Segundo cadastro com o mesmo CPF é recusado com mensagem explícita.
3. **Consulta de cliente é por CPF**; quando não existe, **404**.
4. **Ordem de compra:** CPF do cliente, moeda, **valor na moeda estrangeira** e **agência de retirada**. O total em reais é **calculado pelo sistema**, nunca informado pelo cliente.
5. **Agência de retirada: 4 dígitos**, texto, com zero à esquerda preservado. Tamanho diferente é recusado, sem completar nem truncar.
6. Ordem registrada responde **201** com o comprovante, incluindo **a cotação utilizada** (exigência de auditoria).
7. **Erros: 400** para requisição malformada (campo faltando, formato inválido, valor negativo); **422** para requisição válida que não pode ser processada (moeda fora do catálogo, agência inexistente); **404** para cliente ou ordem não encontrados. Mensagens compreensíveis pelo cliente.
8. **Limite operacional é fase 2.** Registrado em ata: **equivalente a US$ 10.000 por CPF por dia**, acima disso análise e reporte. O desenho deve permitir incluir a regra depois.

## Action items

- **Wagner:** escrever os textos das mensagens de erro dos três casos.
- **Alice:** enviar a norma interna que embasa a ficha cadastral e o limite.
- **Iuri:** manter a validação da ordem isolada, prevendo a regra de limite na fase 2.

## Glossário incrementado

- **Comprovante da ordem:** identificador, cliente, data/hora, moeda, valor em moeda estrangeira, cotação usada, total em reais e agência.
- **Catálogo de moedas:** moedas que o sistema aceita operar. No MVP, dólar e euro.
