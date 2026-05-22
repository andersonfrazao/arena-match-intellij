ALTER TABLE jogo
    ADD COLUMN time_solicitante_id BIGINT;

UPDATE jogo
SET time_solicitante_id = time_mandante_id
WHERE time_solicitante_id IS NULL;

ALTER TABLE jogo
    ALTER COLUMN time_solicitante_id SET NOT NULL,
    ADD CONSTRAINT fk_jogo_solicitante FOREIGN KEY (time_solicitante_id) REFERENCES time (id);
