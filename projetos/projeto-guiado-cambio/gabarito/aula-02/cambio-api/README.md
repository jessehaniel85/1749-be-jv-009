# cambio-api — Aula 2 (pirâmide de testes)

Estado **completo e compilável** ao fim da Aula 2 do projeto guiado **BE-JV-009**. Mesmo comportamento da Aula 1, agora com testes.

- **Stack:** Java 21 · Spring Boot 3.5.9 · Maven · H2 em memória. Sem Docker.
- **Rodar:** `mvn clean test` (12 testes) · `mvn spring-boot:run`

## Onde cada teste cai na pirâmide

```
        ▲  poucos, lentos, alta confiança
       ╱ ╲     ClienteIntegracaoTest        @SpringBootTest + @AutoConfigureMockMvc + H2
      ╱   ╲    ─────────────────────────────────────────────────────────────────────
     ╱     ╲   ClienteControllerTest        @WebMvcTest + MockMvc + @MockitoBean
    ╱       ╲  ─────────────────────────────────────────────────────────────────────
   ╱_________╲ ClienteServiceTest           JUnit 5 + Mockito (sem Spring)
        muitos, rápidos, escopo estreito
```

**Base — `ClienteServiceTest` (unitário).** Não sobe contexto Spring nem banco: o `ClienteRepository` é um `@Mock` e o serviço é montado com `@InjectMocks`. Testa **regra de negócio**: recusar CPF duplicado sem nem chamar `save`, lançar `ClienteNaoEncontradoException` na busca vazia. Roda em milissegundos, então pode haver muitos.

**Meio — `ClienteControllerTest` (fatia web).** `@WebMvcTest(ClienteController.class)` sobe **só** a camada MVC: mapeamento de rota, desserialização JSON, Bean Validation e o `@RestControllerAdvice`. O `ClienteService` é substituído por `@MockitoBean`. Testa o **contrato HTTP** — status, header `Location`, formato do corpo de erro — sem depender de banco.

**Topo — `ClienteIntegracaoTest` (integração).** `@SpringBootTest` + `@AutoConfigureMockMvc` sobe a aplicação inteira contra o H2 em memória e faz `POST` seguido de `GET`. É o único teste que prova que **as peças se encaixam**: mapeamento JPA, constraint de unicidade, transação. É o mais lento — por isso são poucos.

> **A regra prática:** um bug de cálculo se pega na base; um bug de status HTTP, no meio; um bug de mapeamento de coluna, só no topo. Escolher o nível errado custa tempo de build ou confiança falsa.

## Endpoints

| Método | Rota | Resposta |
|---|---|---|
| `POST` | `/clientes` | `201` + `Location` · `400` validação · `409` CPF duplicado |
| `GET` | `/clientes/{cpf}` | `200` · `404` |

Detalhes do estado: **[ESTADO.md](ESTADO.md)** · Board: **[KANBAN.md](KANBAN.md)**
