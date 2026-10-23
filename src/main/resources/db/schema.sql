CREATE TABLE IF NOT EXISTS cliente (
    id TEXT PRIMARY KEY NOT NULL,
    nome TEXT NOT NULL CHECK (length(trim(nome)) > 0)
);

CREATE TABLE IF NOT EXISTS conta (
    id TEXT PRIMARY KEY NOT NULL,
    cliente_id TEXT NOT NULL,
    saldo TEXT NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE TABLE IF NOT EXISTS transacao (
    id TEXT PRIMARY KEY NOT NULL,
    tipo TEXT NOT NULL CHECK (tipo IN ('DEPOSITO', 'SAQUE', 'TRANSFERENCIA')),
    valor TEXT NOT NULL,
    data_hora TEXT NOT NULL,
    conta_origem TEXT NOT NULL,
    conta_destino TEXT,
    resultado TEXT NOT NULL CHECK (resultado = 'CONCLUIDA'),
    FOREIGN KEY (conta_origem) REFERENCES conta(id),
    FOREIGN KEY (conta_destino) REFERENCES conta(id),
    CHECK ((tipo = 'TRANSFERENCIA' AND conta_destino IS NOT NULL AND conta_origem <> conta_destino)
        OR (tipo IN ('DEPOSITO', 'SAQUE') AND conta_destino IS NULL))
);

CREATE INDEX IF NOT EXISTS idx_transacao_origem_data
    ON transacao(conta_origem, data_hora);
CREATE INDEX IF NOT EXISTS idx_transacao_destino_data
    ON transacao(conta_destino, data_hora);
