# Sessão 3 — Cotação, integrações e compliance

- **Projeto:** API de Câmbio — compra de moeda estrangeira com retirada em agência
- **Data:** 19/08/2026 (quarta-feira), 10h00–10h45
- **Plataforma:** videoconferência
- **Participantes:** Renata Vasconcelos (PO, facilitadora), Marcos Yamaguti (Tesouraria — mesa de câmbio), Dra. Alice Nogueira (Compliance/PLD), Iuri Sampaio (Tech lead)
- **Ausentes:** Cláudio Menezes e Wagner Duarte (enviaram os textos de mensagem por escrito)

---

**Renata:** Última sessão. Pauta: de onde vem a cotação, o que fazemos com ela, e a parte da Alice — auditoria, dado pessoal, retenção. Marcos, você é o motivo desta reunião: de onde vem o preço?

**Marcos:** Da mesa. A mesa forma o preço do varejo a partir do mercado e publica num sistema interno, e a agência consulta lá. Atualizamos várias vezes ao dia.

**Iuri:** Marcos, deixa eu contar o que eu tentei. Existe API pública de cotação na internet, de graça. Chamei de dentro daqui e não passa: o proxy corporativo bloqueia. E mesmo liberada, eu não usaria como fonte oficial — serviço gratuito, sem contrato, sem compromisso de disponibilidade. Se cair, ninguém me deve nada.

**Marcos:** Nem poderia ser oficial. Se o cliente fecha uma ordem numa cotação que veio de um site e a mesa não reconhece o preço, quem cobre a diferença? Eu. A fonte oficial é a mesa.

**Renata:** Então a cotação vem de uma tabela nossa, alimentada pela mesa?

**Marcos:** Isso. E eu preciso atualizar essa tabela, não pedir pra TI toda vez.

**Iuri:** Fechado, e de um jeito específico: vou definir um **contrato** para "de onde vem a cotação" — uma porta de entrada única. Por trás dela, hoje, a tabela interna alimentada pela mesa. Se um dia contratarem um provedor de verdade, ele entra atrás da mesma porta e o resto do sistema não fica sabendo.

**Renata:** Isso não é over-engineering pra uma tabelinha?

**Iuri:** Seria, se a fonte fosse definitiva. Mas é a única parte do sistema que a gente já sabe que vai trocar. E em teste eu ligo a fonte externa por configuração e demonstro o fluxo sem depender da mesa.

**Marcos:** Só me garanta que em produção o padrão é a tabela.

**Iuri:** É a tabela. A externa só liga se alguém ligar de propósito, e fora da rede.

**Alice:** E quem atualiza fica registrado? Preço é a coisa mais sensível dessa história.

**Marcos:** Perfil de tesouraria, e só.

**Alice:** Então registrado: quem alterou, qual moeda, valor anterior, valor novo, quando.

**Renata:** Agora a pergunta que eu vim fazer: a cotação **vale por quanto tempo**? O cliente consulta às dez e decide às onze. Que preço vale?

**Marcos:** O do momento em que ele registra a ordem. Não o da consulta.

**Renata:** Então a consulta é… informativa?

**Marcos:** É vitrine. Enquanto ele não registra, não tem preço.

**Renata:** E dá pra travar o preço por meia hora?

**Marcos:** Dá, mas tem nome e custo: é posição em aberto. Se eu garanto um preço por meia hora, tenho que me proteger no mercado por essa meia hora — isso é hedge. Não é linha de código, é operação financeira. Não no MVP.

**Renata:** Fechado: **sem trava de preço.** Vale a cotação do instante do registro.

**Marcos:** E ela fica **gravada dentro da ordem**. Inegociável. Se dois dias depois alguém questionar o valor, eu quero olhar a ordem e ver a cotação e a hora exata em que foi aplicada. Não quero recalcular nada.

**Alice:** Assino embaixo. Ordem é documento; documento não muda de valor depois.

**Renata:** Anotado com estrela. E o spread?

**Marcos:** O que a mesa publica pro varejo **já vem com o spread embutido**. Vocês pegam o número e usam.

**Iuri:** Vai continuar assim?

**Marcos:** Provavelmente não. O spread é diferente por moeda — dólar e euro não têm a mesma margem — e um dia vão querer diferenciar por canal. Se vier, o cálculo acontece aí dentro.

**Iuri:** Então deixo o cálculo da operação **isolado e por moeda**, mesmo que hoje as duas façam a mesma conta. Quando a regra divergir, é uma peça nova, não uma cirurgia.

**Renata:** E a conta? Valor em moeda estrangeira vezes cotação?

**Marcos:** Certo, e presta atenção, porque é aqui que projeto costuma errar. A cotação tem **quatro casas decimais**. O total da operação tem **duas casas**, é real. E o arredondamento é **arredondamento bancário**.

**Renata:** Arredondamento bancário é…?

**Marcos:** Meio para o par. Quando sobra exatamente meio centavo, vai para o vizinho par em vez de subir sempre: dois e cinco vira dois, três e cinco vira quatro.

**Renata:** Por que não arredondar pra cima e pronto?

**Marcos:** Porque pra cima é sempre a favor do banco. Numa operação ninguém nota; em cem mil, virou diferença sistemática a favor da casa — e isso aparece em conciliação, em reclamação e em auditoria. Pra cima não é conservador, é enviesado. Anota: **arredondamento bancário, meio para o par, duas casas no total**. Repete pro time, porque some fácil.

**Renata:** Anotei com essas palavras. Alice, sua parte.

**Alice:** Três coisas. Primeira, dado pessoal: nome, CPF, nascimento, estado civil, sexo — tratamento sob obrigação legal, pela ficha cadastral de câmbio. Na prática, para o time: não jogar CPF inteiro em log, não expor dado do cliente em mensagem de erro.

**Iuri:** Anotado.

**Alice:** Segunda, trilha: preciso saber **quem registrou** cada ordem e **quem consultou** um cadastro.

**Renata:** E a terceira?

**Alice:** Retenção, e essa eu quero cravada. A ordem de câmbio deve ser **retida por cinco anos** contados da data da ordem. **Cinco anos.** Anota o número, Renata, porque eu já vi requisito de retenção virar "pelo prazo legal" na compilação e ninguém saber mais qual era o prazo. **Cinco.**

**Renata:** Cinco anos da data da ordem. Anotado com o número.

**Alice:** No MVP, o mínimo é que nada seja apagado. Descarte a gente faz depois — descartar cedo é pior do que não descartar.

**Renata:** Combinado. Iuri, fecha com as histórias técnicas que você pediu na primeira reunião.

**Iuri:** Cinco, cada uma com o porquê. Um: **teste automatizado desde o início**, nos três níveis — a regra sozinha, o contato pela API e o sistema de ponta a ponta. Sem isso eu não mexo no código com segurança, e a gente vai mexer muito. Dois: **o contrato da cotação**, que acabamos de decidir. Três: quando os três domínios crescerem, **separar em serviços** — cliente, cotação e câmbio —, cada um com sua base. Quatro: separados, eles precisam **se achar sem URL chumbada em arquivo**, senão troca de ambiente vira incidente. Cinco: **refatorar** e aplicar **padrões conhecidos** onde resolvem problema real, como o cálculo por moeda. Padrão pra enfeitar, não.

**Renata:** Anotei as cinco. Compilo tudo em histórias hoje à noite — o time começa segunda e eu não quero segurar ninguém esperando documento. Se alguma coisa ficar estranha ou faltando, a fonte são estas transcrições: vocês falaram, está gravado. Obrigada, pessoal.

**Marcos:** Só não erra o arredondamento.

---

## Decisões da Sessão 3

1. **A fonte oficial da cotação é a mesa de câmbio.** O sistema lê de uma **tabela interna** alimentada pela Tesouraria — padrão em produção.
2. A origem fica atrás de um **contrato único**. Uma fonte externa pode ser plugada atrás do mesmo contrato, **por configuração**, para teste ou demonstração; nunca como padrão. A API pública testada é **bloqueada pela rede corporativa** e não tem compromisso de disponibilidade.
3. A Tesouraria **atualiza a cotação** (perfil restrito). Toda alteração é registrada: quem, moeda, valor anterior, valor novo, quando.
4. **Não há trava de preço.** Vale a cotação do **instante do registro da ordem**; a consulta é informativa.
5. A **cotação aplicada fica gravada na ordem**, com data e hora. Ordem é documento: não se recalcula depois.
6. **Spread já vem embutido** na cotação publicada. Como tende a variar por moeda, o **cálculo da operação fica isolado por moeda**.
7. **Precisão:** cotação com **4 casas**; total com **2 casas**, em **arredondamento bancário (meio para o par)**. Arredondar sempre para cima foi explicitamente rejeitado por ser enviesado.
8. **Dado pessoal** sob obrigação legal: sem CPF completo em log, sem dado do cliente em mensagem de erro. **Trilha:** quem registrou cada ordem e quem consultou cada cadastro.
9. **Retenção: 5 anos** contados da data da ordem. No MVP nada é apagado; descarte fica para depois.
10. **Histórias técnicas acordadas:** testes nos três níveis, contrato da cotação, separação em serviços com bases próprias, localização entre serviços sem URL fixa, refatoração e padrões aplicados a problema real.

## Action items

- **Marcos:** enviar a tabela de cotações vigente (USD e EUR) e confirmar a precisão de 4 casas.
- **Alice:** enviar a norma de retenção com o prazo de 5 anos referenciado.
- **Iuri:** desenhar o contrato da fonte de cotação e listar as histórias técnicas no board.
- **Renata:** compilar as user stories e circular para validação.
