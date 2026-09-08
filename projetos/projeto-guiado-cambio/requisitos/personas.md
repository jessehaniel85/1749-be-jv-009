# Personas — Projeto API de Câmbio

Fichas das pessoas que participam das reuniões de elicitação. Use-as para entender de onde vem cada fala nas transcrições e por que cada uma defende o que defende. As vozes são consistentes ao longo das três sessões.

> **Contexto institucional:** todos trabalham em **uma instituição financeira de grande porte**, na Superintendência de Produtos de Câmbio e áreas correlatas. As reuniões acontecem em agosto de 2026, semanais, por videoconferência. Nenhum nome de banco real aparece neste material — nem deve aparecer no código.

---

## Renata Vasconcelos — Product Owner, Produto de Câmbio

- **Papel:** dona do produto e facilitadora das reuniões. Conduz a pauta, traduz negócio ↔ técnico e, ao final, **compila as user stories** que viram a primeira fonte de verdade para o time.
- **Objetivos:** colocar no ar um MVP de compra de moeda estrangeira pelo app que tire o pedido do telefone e do papel; sair de cada reunião com decisão e responsável; não travar o time esperando documento perfeito.
- **Jeito de falar:** organizada, recapitula bastante, fecha bloco com pergunta ("deixa eu confirmar então..."). Usa muito "anotei", "fechado", "isso é fase 2". Vai montando o glossário durante a reunião.
- **Vieses (importante):** com prazo curto, **simplifica ao transcrever**. Transforma nuance técnica e número exato em frase curta e redonda, e às vezes generaliza uma decisão para além do que foi combinado. É competente — os deslizes vêm da pressa de compilar. **É a fonte natural das divergências entre as user stories e as transcrições**, e ela mesma avisa isso mais de uma vez.

## Cláudio Menezes — Gerente de agência (agência piloto)

- **Papel:** representa a ponta que **entrega a moeda**. É no caixa dele que o cliente aparece com a ordem, paga e retira as cédulas.
- **Objetivos:** que a ordem chegue à agência com dados que o caixa consiga usar — agência certa, cliente identificável, valor fechado. Não quer cliente indo à agência duas vezes nem discussão de balcão sobre valor.
- **Jeito de falar:** concreto, cheio de caso do dia a dia. "Ontem chegou um cliente aqui...", "no balcão isso não funciona", "o caixa não vai adivinhar". Fala em números da operação (número de agência, quantidade de cédula, horário do malote).
- **Vieses:** raciocina a partir da exceção que já viu acontecer; tende a pedir campo a mais "por garantia". **É a voz canônica dos formatos operacionais** — número de agência, o que o caixa precisa ver no comprovante.

## Marcos Yamaguti — Analista de Tesouraria (mesa de câmbio) *(entra na Sessão 3)*

- **Papel:** representa a mesa que **forma o preço**. É de lá que sai a cotação que o cliente vê e o spread que remunera a operação.
- **Objetivos:** que o sistema use a cotação **oficial da mesa**, com a precisão certa, e que ninguém invente regra de preço no meio do caminho. Não quer exposição cambial não coberta.
- **Jeito de falar:** preciso com número e com nome de coisa ("arredondamento bancário", "meio para o par", "spread", "hedge"). Corrige com educação, mas corrige. Repete o número quando percebe que não foi anotado.
- **Vieses:** desconfia de fonte de preço que não seja a mesa; acha que "detalhe de centavo" não é detalhe. **É a voz canônica das regras de cálculo** — precisão, arredondamento, congelamento da cotação.

## Dra. Alice Nogueira — Compliance e Prevenção à Lavagem de Dinheiro (PLD)

- **Papel:** guardiã regulatória. Valida ficha cadastral, limites, tratamento de dado pessoal, trilha de auditoria e retenção.
- **Objetivos:** que a operação de câmbio seja rastreável ponta a ponta e que o dado do cliente seja tratado dentro da LGPD. Evitar achado de auditoria.
- **Jeito de falar:** cautelosa e exata. "Isso é dado pessoal, então...", "eu preciso que isso conste em ata", "me dá o número, não o 'mais ou menos'". Não tem pressa: prefere travar agora a remediar depois.
- **Vieses:** conservadora — na dúvida, pede o controle mais rígido. Insiste em **números exatos** (prazo de retenção, limite) e em saber **quem** fez o quê. Pode soar como freio para quem quer entregar rápido.

## Wagner Duarte — Coordenador de Atendimento e Canais

- **Papel:** representa quem atende o cliente antes e depois da ordem — central telefônica, chat do app, retaguarda.
- **Objetivos:** que o cliente resolva sozinho no app e, quando der erro, **entenda o que fazer** sem ligar. Reduzir contato repetido sobre o mesmo pedido.
- **Jeito de falar:** anedótico, fala pela boca do cliente e do atendente. "O cliente lê isso e liga pra gente", "'erro 500' pra ele não quer dizer nada", "ontem mesmo teve um caso". Puxa sempre para a mensagem que aparece na tela.
- **Vieses:** foca no caso de uso imediato do atendimento e pode subestimar o custo técnico do que pede. É quem **levanta os casos de erro** e cobra texto de mensagem, não só código de status.

---

## Time de entrega (não é stakeholder — é quem recebe o requisito)

## Iuri Sampaio — Tech lead do time de desenvolvimento

- **Papel:** lidera tecnicamente o time que vai construir a API. Nas reuniões, esclarece consequência técnica de decisão de negócio e negocia espaço no backlog para **histórias técnicas (enabler stories)**: testes, contrato de serviço, separação em serviços, refatoração, padrões.
- **Objetivos:** um sistema que comece simples e possa crescer sem virar bola de lama; contratos explícitos com quem está fora do time; testes que permitam refatorar sem medo.
- **Jeito de falar:** pensa em voz alta sobre falha ("e se o provedor cair no meio do dia?"), usa analogia para explicar conceito a quem é de negócio, evita citar marca de fornecedor. Distingue com cuidado coisas parecidas (400 × 422, "não entendi" × "entendi e não posso aceitar").
- **Vieses:** otimiza para evolução e desacoplamento; às vezes entra em detalhe que a Renata precisa cortar. **É a voz canônica dos fatos técnicos** — quando a user story diverge do que o Iuri disse, o erro costuma estar na story.
