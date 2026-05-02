-- 1. Cria a Sequence explicitamente (inicia em 1, incrementa de 1 em 1)
CREATE SEQUENCE time_seq START 1 INCREMENT 1;

-- 2. Cria a tabela usando BIGINT e o valor default da sequence
CREATE TABLE time (
    id BIGINT PRIMARY KEY DEFAULT nextval('time_seq'), -- Aqui estava o erro (era SERIAL)
    nome_responsavel VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    whatsapp VARCHAR(20),
    nome_time VARCHAR(255) NOT NULL,
    mando_campo VARCHAR(50),
    escudo_url VARCHAR(500),
    cep VARCHAR(10),
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    complemento VARCHAR(255),
    cidade VARCHAR(100),
    uf VARCHAR(2),
    regiao VARCHAR(50),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. (Opcional) Garante que se apagar a tabela, a sequence morre junto
ALTER SEQUENCE time_seq OWNED BY time.id;