# Estado após a Aula 2 — a rede de segurança de testes

**Foco:** nenhum comportamento novo. O que mudou é que agora existe **prova automatizada** de que o comportamento da Aula 1 continua de pé — é isso que torna as refatorações das Aulas 7 e 8 possíveis.

## O que funciona

- Tudo da Aula 1 (`POST /clientes`, `GET /clientes/{cpf}`, 400/404/409).
- **12 testes** em três níveis, todos verdes com `mvn test`:

| Teste | Nível | Sobe o quê | Casos |
|---|---|---|---|
| `ClienteServiceTest` | unitário | nada (Mockito puro) | cadastro ok, dados repassados, CPF duplicado, busca ok, busca inexistente |
| `ClienteControllerTest` | fatia web (`@WebMvcTest`) | MVC + advice, serviço mockado | 201 + `Location`, 400 com 2 erros, 409, 200, 404 |
| `ClienteIntegracaoTest` | integração (`@SpringBootTest`) | tudo + H2 real | POST→GET, CPF duplicado no fluxo real |

## Como rodar

```bash
export JAVA_HOME=$HOME/.sdkman/candidates/java/21-open
mvn clean test                                    # 12 testes
mvn test -Dtest=ClienteServiceTest                # só a base da pirâmide
```

## Perguntas para a turma

- O `ClienteServiceTest` não conhece HTTP e o `ClienteControllerTest` não conhece banco. Que bug **nenhum dos dois** pegaria — e qual teste pega?
- Por que `@MockitoBean` no `@WebMvcTest` e `@Mock` no unitário? O que cada um substitui?
- Se `ClienteIntegracaoTest` não tivesse `@Transactional`, o que aconteceria com o segundo teste da classe?

## Próxima aula

**US-04 e US-05**: o monólito cresce — domínios `cotacao` e `ordem` no mesmo projeto, com `OrdemService` chamando `ClienteService` e `CotacaoService` por injeção direta. Os testes desta aula seguram a mudança.
