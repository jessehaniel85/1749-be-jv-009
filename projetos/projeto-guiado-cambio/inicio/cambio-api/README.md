# cambio-api — esqueleto de partida (Aula 1)

Ponto de partida do projeto guiado **BE-JV-009**, construído ao vivo aula a aula. Este projeto **já compila** — os `// TODO (Aula N)` marcam exatamente o que se escreve em cada encontro.

- **Stack:** Java 21 · Spring Boot 3.5.9 · Maven · H2 em memória. Sem Docker, sem infra externa.
- **Pacote raiz:** `br.com.ada.cambio` · um sub-pacote por domínio (`cliente`, e depois `cotacao` e `ordem`) e, dentro de cada um, por camada: `api`, `dominio`, `infra`.

## Rodar

```bash
export JAVA_HOME=$HOME/.sdkman/candidates/java/21-open
mvn clean compile      # compila o esqueleto
mvn spring-boot:run    # sobe em http://localhost:8080 (H2 console em /h2-console)
```

## O que já existe e o que falta

| Arquivo | Estado |
|---|---|
| `CambioApiApplication` · `application.yml` | prontos |
| `EstadoCivil` · `Sexo` · exceções · `RespostaDeErro` · `ErroDeCampo` | prontos |
| `Cliente` | só `id` e `nome` — faltam `cpf`, `dataNascimento`, `estadoCivil`, `sexo` |
| `ClienteRepository` | faltam `findByCpf` e `existsByCpf` |
| `ClienteService` | falta a recusa de CPF duplicado e a busca por CPF |
| `ClienteRequest` · `ClienteResponse` | faltam as anotações de validação e os campos de saída |
| `ClienteController` | faltam `@Valid`, o `201` com `Location` e o `404` |
| `TratadorDeErros` | vazio — os handlers são o fecho da Aula 1 |

```bash
# ver tudo o que falta, na ordem
grep -rn "TODO" src/main/java
```

## Como o projeto cresce

Consulte o board em **[KANBAN.md](KANBAN.md)** — as dez histórias do módulo estão em **TO DO**. A Aula 1 puxa **US-01** e **US-02**; cada aula seguinte fecha as suas. O estado completo ao fim de cada aula fica em `../gabarito/aula-0X/`.
