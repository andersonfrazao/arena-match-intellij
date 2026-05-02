-- 1. Cria a Sequence para disponibilidade
CREATE SEQUENCE disponibilidade_seq START 1 INCREMENT 1;

-- 2. Cria a tabela usando BIGINT e a sequence
CREATE TABLE disponibilidade (
    id BIGINT PRIMARY KEY DEFAULT nextval('disponibilidade_seq'),
    time_id BIGINT NOT NULL,
    dia_semana VARCHAR(20) NOT NULL,
    hora_inicio VARCHAR(5) NOT NULL,
    hora_fim VARCHAR(5) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    
    -- Chave estrangeira ligando ao Time
    CONSTRAINT fk_time FOREIGN KEY (time_id) REFERENCES time (id)
);

-- 3. Vincula a sequence à coluna (boa prática)
ALTER SEQUENCE disponibilidade_seq OWNED BY disponibilidade.id;