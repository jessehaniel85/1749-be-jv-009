# Estado após a Aula 1 — o monólito nasce com o domínio Cliente

**Foco:** transformar as duas primeiras histórias do Kanban em código que roda, com validação de entrada e tratamento de erro centralizado.

## O que funciona

- **`POST /clientes`** → **201** com header `Location: /clientes/{cpf}` e o cliente no corpo.
- **Validação de entrada** (`jakarta.validation` no `ClienteRequest`): `nome` obrigatório, `cpf` com exatamente 11 dígitos, `dataNascimento` no passado, `estadoCivil` e `sexo` obrigatórios. Violação → **400** com a lista de campos com problema.
- **CPF já cadastrado** → **409** (`CpfDuplicadoException`).
- **`GET /clientes/{cpf}`** → **200** ou **404** (`ClienteNaoEncontradoException`).
- **`@RestControllerAdvice`** (`TratadorDeErros`) concentra a tradução exceção → status. Nenhum `try/catch` nos controllers.
- Persistência em **H2 em memória**, console em `/h2-console`.

## O que ainda NÃO existe

- Testes automatizados (Aula 2 — US-03).
- Domínios `cotacao` e `ordem` (Aula 3 — US-04 e US-05).

## Como rodar

```bash
export JAVA_HOME=$HOME/.sdkman/candidates/java/21-open   # Java 21
mvn clean test          # build
mvn spring-boot:run     # sobe em http://localhost:8080
```

Console do H2: <http://localhost:8080/h2-console> — JDBC URL `jdbc:h2:mem:cambio`, usuário `sa`, senha vazia.

## Exemplos cURL

```bash
# 201 — cadastro válido
curl -i -X POST localhost:8080/clientes -H 'Content-Type: application/json' -d '{
  "nome": "Marina Alcântara",
  "cpf": "43488428095",
  "dataNascimento": "1991-04-17",
  "estadoCivil": "SOLTEIRO",
  "sexo": "FEMININO"
}'
# → HTTP/1.1 201, Location: http://localhost:8080/clientes/43488428095

# 400 — CPF com formato inválido e nome em branco
curl -i -X POST localhost:8080/clientes -H 'Content-Type: application/json' -d '{
  "nome": "",
  "cpf": "434.884.280-95",
  "dataNascimento": "2099-01-01",
  "estadoCivil": "SOLTEIRO",
  "sexo": "FEMININO"
}'
# → HTTP/1.1 400 com {"status":400,"mensagem":"Requisição inválida","erros":[...]}

# 409 — mesmo CPF de novo
curl -i -X POST localhost:8080/clientes -H 'Content-Type: application/json' -d '{
  "nome": "Outra Pessoa", "cpf": "43488428095", "dataNascimento": "1985-02-02",
  "estadoCivil": "CASADO", "sexo": "MASCULINO"
}'
# → HTTP/1.1 409

# 200 — consulta por CPF
curl -i localhost:8080/clientes/43488428095

# 404 — CPF sem cadastro
curl -i localhost:8080/clientes/00000000000
```

## Perguntas para a turma

- Por que a validação está no `ClienteRequest` e não na entidade `Cliente`?
- CPF duplicado é **409** ou **400**? O que muda para quem consome a API?
- O `Location` deveria apontar para `/clientes/{id}` ou `/clientes/{cpf}`? Qual é o identificador público deste recurso?

## Próxima aula

**US-03 — rede de segurança de testes**: unitário do serviço (Mockito), teste de controller (`@WebMvcTest`) e um de integração (`@SpringBootTest` + H2). Sem eles, as refatorações das Aulas 7 e 8 seriam apostas.
