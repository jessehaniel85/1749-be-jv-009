# Kanban — API de Câmbio (BE-JV-009)

Board vivo do projeto guiado. **Mover card é parte da aula**, não cerimônia decorativa.
Cada história vira código em uma aula; o estado do board sempre bate com o estado da pasta.

**WIP limite = 1** em DOING: começar tudo é a forma mais rápida de não entregar nada.

**Definição de Pronto (DoD)**
- [ ] `mvn clean test` verde
- [ ] endpoint devolve exatamente os status combinados no critério
- [ ] erro tratado no `@RestControllerAdvice` (nada de stack trace vazando)
- [ ] teste cobrindo o caminho feliz e pelo menos um de erro (a partir da Aula 2)
- [ ] `README.md` / `ESTADO.md` atualizados

> **Estado atual:** fim da **Aula 2** — o mesmo comportamento da Aula 1, agora com rede de segurança.

---

## TO DO

### US-04 — Consultar cotação da moeda
**Como** cliente **quero** ver a cotação de USD ou EUR **para** decidir se compro agora.
Critérios: `GET /cotacoes/{moeda}` → **200** com `moeda`, `valorCotacao` (4 casas) e `dataHora`; sigla fora de USD/EUR → **422** (`MoedaNaoSuportadaException`); tabela local semeada por `data.sql`.
`Aula 3`

### US-05 — Registrar ordem de compra
**Como** cliente cadastrado **quero** registrar uma ordem de compra **para** retirar a moeda na agência.
Critérios: `POST /ordens` com `cpf`, `moeda`, `valorMoedaEstrangeira`, `numeroAgenciaRetirada` (4 dígitos) → **201** no contrato snake_case do módulo; `valorTotalOperacao = valor × cotação` (2 casas, `HALF_EVEN`); CPF sem cadastro → **404**; moeda ou agência inválida → **422**; `GET /ordens/{id}` → **200**/**404**.
`Aula 3`

### US-06 — Contrato do provedor de cotação
**Como** arquitetura **queremos** um contrato único de cotação **para** trocar a origem do dado sem tocar no domínio.
Critérios: interface `CotacaoProvider` com duas implementações (`local` lendo H2, `externo` chamando a awesomeapi via `RestClient` + Adapter), escolha por `cotacao.provedor`; `PUT /cotacoes/{moeda}` simula variação no provedor local; contrato documentado em `docs/contrato-cotacao.md`.
`Aula 4`

### US-07 — Quebrar o monólito em serviços
**Como** time **queremos** separar `cliente`, `cotacao` e `cambio` em serviços **para** evoluir e implantar cada um por conta própria.
Critérios: 3 projetos Maven com bases H2 segregadas (8081/8082/8083); `cambio-service` chama os outros por `RestClient` com URL fixa em `application.yml`; cada serviço com seus testes.
`Aula 5`

### US-08 — Descoberta de serviços e cliente declarativo
**Como** time **queremos** parar de fixar URL no YAML **para** que os serviços se encontrem sozinhos.
Critérios: `discovery-server` (Eureka, 8761); os 3 serviços registrados; `cambio-service` consome os demais por `@FeignClient` em vez de `RestClient`.
`Aula 6`

### US-09 — Refatoração Clean Code + SOLID
**Como** time **queremos** limpar o código **para** que ele aguente as próximas mudanças.
Critérios: sem números mágicos (11 do CPF, 4 da agência, escala 2); injeção por construtor em todo lugar; `OrdemService` com responsabilidades separadas (validar/calcular/persistir); interfaces pequenas; dependência da abstração. Testes seguem verdes — nenhum comportamento muda.
`Aula 7`

### US-10 — Design Patterns aplicados
**Como** time **queremos** nomear as soluções com padrões **para** conversar em um vocabulário comum.
Critérios: **Strategy** de cálculo por moeda, **Facade** de orquestração, discussão de **Singleton** × escopo de bean, **Adapter** já existente nomeado, **Builder** opcional no DTO da ordem; um ADR curto por padrão em `docs/adr/`.
`Aula 8`

---

## DOING

_(vazio — WIP livre para o próximo card)_

---

## DONE

### US-01 — Cadastrar cliente
**Como** pessoa interessada em comprar moeda **quero** me cadastrar **para** poder operar.
Critérios: `POST /clientes` com `nome`, `cpf` (11 dígitos), `dataNascimento`, `estadoCivil`, `sexo` → **201** com `Location`; campo inválido → **400** com a lista de erros; CPF repetido → **409**.
`Aula 1`

### US-02 — Consultar cliente por CPF
**Como** atendente **quero** consultar um cliente pelo CPF **para** confirmar o cadastro antes da ordem.
Critérios: `GET /clientes/{cpf}` → **200** com os dados; CPF sem cadastro → **404** tratado pelo `@RestControllerAdvice`.
`Aula 1`

### US-03 — Rede de segurança de testes
**Como** time **queremos** testes automatizados **para** refatorar sem medo nas aulas 7 e 8.
Critérios: `ClienteServiceTest` (JUnit 5 + Mockito), `ClienteControllerTest` (`@WebMvcTest` + MockMvc), `ClienteIntegracaoTest` (`@SpringBootTest` + H2); `mvn test` verde; README explica a pirâmide.
`Aula 2`

