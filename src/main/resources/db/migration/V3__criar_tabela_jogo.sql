CREATE SEQUENCE jogo_seq START 1 INCREMENT 1;

CREATE TABLE jogo (
    id BIGINT PRIMARY KEY DEFAULT nextval('jogo_seq'),
    time_mandante_id BIGINT NOT NULL,
    time_visitante_id BIGINT NOT NULL,
    data_jogo DATE NOT NULL,
    hora_inicio VARCHAR(5) NOT NULL,
    hora_fim VARCHAR(5) NOT NULL,
    status VARCHAR(20) NOT NULL,
    
    CONSTRAINT fk_jogo_mandante FOREIGN KEY (time_mandante_id) REFERENCES time (id),
    CONSTRAINT fk_jogo_visitante FOREIGN KEY (time_visitante_id) REFERENCES time (id)
);

ALTER SEQUENCE jogo_seq OWNED BY jogo.id;