-- Semente da tabela local de cotações.
-- Roda a cada boot (H2 em memória) graças a spring.jpa.defer-datasource-initialization: true,
-- que adia o data.sql para DEPOIS de o Hibernate criar as tabelas.
--
-- MERGE (e não INSERT) para a semente ser IDEMPOTENTE: uma suíte de testes sobe vários
-- contextos Spring na mesma JVM, e todos compartilham o mesmo banco em memória. Com INSERT,
-- o segundo contexto estouraria violação de chave primária.
--
-- Estes são os valores usados nos exemplos do módulo; PUT /cotacoes/{moeda} (Aula 4) altera.
MERGE INTO cotacoes (id, moeda, valor_cotacao, data_hora) KEY (id)
    VALUES (1, 'USD', 5.4321, CURRENT_TIMESTAMP);
MERGE INTO cotacoes (id, moeda, valor_cotacao, data_hora) KEY (id)
    VALUES (2, 'EUR', 6.5857, CURRENT_TIMESTAMP);
