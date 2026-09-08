# Estado ao fim da Aula 7 — mesmo comportamento, outro código

## A regra da aula

**Nenhuma resposta HTTP mudou.** Mesmos endpoints, mesmos status, mesmo corpo.
Os cURLs da Aula 6 rodam aqui sem alterar um byte — e é por isso que dá para chamar
o que fizemos de **refatoração**.

O que mudou foi a estrutura interna. Está tudo destrinchado, com trecho antes/depois
por princípio, em **`docs/antes-depois-solid.md`**.

## O que mudou, em uma tabela

| | Aula 6 | Aula 7 |
|---|---|---|
| `OrdemService` | validava, integrava, calculava e persistia | orquestra; validar e calcular são de outros |
| Números `11`, `4`, `2` | soltos em `matches("\d{4}")`, `setScale(2, ...)` | `RegrasDeCambio.TAMANHO_AGENCIA`, `ESCALA_MONETARIA` |
| Porta para o cliente | `@FeignClient ClienteClient` usado direto pelo domínio | `dominio.ConsultaCliente` ← `infra.ClienteClientAdapter` → `infra.ClienteFeignClient` |
| `TratadorDeErros` | importava `feign.FeignException` | só exceções de domínio |
| `ClienteService` (8081) | classe concreta | implementa `ConsultaCliente` + `CadastroCliente` |
| `CotacaoProvider` | contrato implícito | pré/pós-condições no Javadoc |
| Assinatura de `registrar` | 4 parâmetros, 2 deles `String` | um `NovaOrdem` |

## Injeção por construtor: o antes/depois deste projeto

O projeto **já usava injeção por construtor desde a Aula 1** — não havia `@Autowired` em
campo para remover. Isso não torna o tema menos importante: é o antipadrão nº 1 que a turma
vai encontrar em código legado.

O contra-exemplo completo, com os quatro motivos para evitá-lo, está em
**`docs/antes-depois-solid.md`, seção 0**. Em uma linha: `@Autowired` em campo impede
`final`, esconde o acoplamento crescente e obriga o teste a subir Spring para conseguir
instanciar a classe.

Em aula, vale **escrever a versão errada ao vivo** e tentar testá-la — o `NullPointerException`
que aparece explica melhor que qualquer slide.

## Como rodar (idêntico à Aula 6)

```bash
cd aula-07
mvn -pl discovery-server spring-boot:run   # 8761 — primeiro
mvn -pl cliente-service  spring-boot:run   # 8081
mvn -pl cotacao-service  spring-boot:run   # 8082
mvn -pl cambio-service   spring-boot:run   # 8083
```

Plano C (sem Eureka), três terminais:

```bash
mvn -pl <modulo> spring-boot:run -Dspring-boot.run.profiles=plano-c
```

Na rede da Caixa, acrescente `-s ../../../../ambiente/settings.xml`.

## Prova de que o comportamento não mudou

```bash
curl -i -X POST http://localhost:8081/clientes -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Souza","cpf":"43488428095","dataNascimento":"1990-05-10",
       "estadoCivil":"SOLTEIRO","sexo":"FEMININO"}'                      # 201

curl -i -X POST http://localhost:8083/ordens -H 'Content-Type: application/json' \
  -d '{"cpf_cliente":"43488428095","tipo_moeda":"EUR",
       "valor_moeda_estrangeira":100.00,"numero_agencia_retirada":"7057"}'
# 201 → "valor_total_operacao": 658.57   (byte a byte igual ao da Aula 6)
```

| Caso | Aula 6 | Aula 7 |
|---|---|---|
| CPF com 3 dígitos | 400 | 400 |
| CPF válido não cadastrado | 404 | 404 |
| `tipo_moeda: "JPY"` | 422 | 422 |
| agência com 3 dígitos | 422 | 422 |
| `cliente-service` derrubado | 503 | 503 |

## A demonstração que fecha a aula

Rode isto ao vivo, projetado:

```bash
grep -rl "import feign" aula-06/cambio-service/src/main
# → api/TratadorDeErros.java          a lib de transporte alcançou a BORDA

grep -rl "import feign" aula-07/cambio-service/src/main
# → infra/ClienteClientAdapter.java
# → infra/CotacaoClientAdapter.java   ela para na INFRA
```

**Pergunte à turma:** *quantos arquivos eu preciso reescrever para trocar Feign por gRPC?*
Na Aula 6: três, em três camadas. Aqui: dois, na mesma camada — e o domínio nem fica sabendo.

Segunda pergunta, mais difícil: *valeu o preço?* Nasceram duas interfaces, dois records de
domínio e dois Adapters. **A resposta honesta é "depende"** — e o critério está no ADR-005:
a inversão se paga quando o domínio tem regra própria que vai sobreviver à biblioteca de
transporte. Num CRUD que só repassa dados, seria cerimônia.

## Testes

```bash
cd aula-07 && mvn clean test
```

| Módulo | Novidades da Aula 7 |
|---|---|
| `cliente-service` | `ClienteControllerTest` mocka **duas** interfaces (`CadastroCliente`, `ConsultaCliente`) em vez da classe |
| `cotacao-service` | inalterado — o `CotacaoProvider` já estava certo desde a Aula 4 |
| `cambio-service` | **novos:** `ValidadorDeOrdemTest`, `CalculadoraDeOperacaoTest`, `ClienteClientAdapterTest`, `CotacaoClientAdapterTest` |

Duas coisas para mostrar nos testes:

1. `ValidadorDeOrdemTest` e `CalculadoraDeOperacaoTest` **não têm mock nenhum e não sobem
   Spring**. É o retorno concreto do SRP: regra pura vira teste puro.
2. `OrdemServiceTest` **não importa `feign`**. Os únicos testes que ainda constroem
   `FeignException` são os dos Adapters — que é exatamente onde a tradução mora.

E os testes de controller mantêm as **mesmas asserções** da Aula 6. Se um deles tivesse
mudado de status esperado, a refatoração teria falhado no seu único critério.
