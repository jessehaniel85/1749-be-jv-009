# cambio-api — Aula 1 (monólito, domínio Cliente)

Projeto guiado do módulo **BE-JV-009**, construído ao vivo. Este diretório é o estado **completo e compilável** ao fim da Aula 1.

- **Stack:** Java 21 · Spring Boot 3.5.9 · Maven · H2 em memória. Sem Docker, sem infra externa.
- **Pacote raiz:** `br.com.ada.cambio`, um sub-pacote por domínio (`cliente`) e, dentro dele, por camada: `api` (controller e DTOs), `dominio` (entidade, serviço, exceções), `infra` (repositório).

## Endpoints

| Método | Rota | Resposta |
|---|---|---|
| `POST` | `/clientes` | `201` + `Location` · `400` validação · `409` CPF duplicado |
| `GET` | `/clientes/{cpf}` | `200` · `404` |

## Rodar

```bash
mvn clean test        # build
mvn spring-boot:run   # http://localhost:8080
```

## Onde olhar

```
src/main/java/br/com/ada/cambio/
├── CambioApiApplication.java
├── cliente/
│   ├── api/          ClienteController · ClienteRequest · ClienteResponse
│   ├── dominio/      Cliente · ClienteService · EstadoCivil · Sexo · exceções
│   └── infra/        ClienteRepository
└── comum/api/        TratadorDeErros · RespostaDeErro · ErroDeCampo
```

Detalhes do que funciona, exemplos cURL e o que vem a seguir: **[ESTADO.md](ESTADO.md)**.
Backlog e board do projeto: **[KANBAN.md](KANBAN.md)**.
