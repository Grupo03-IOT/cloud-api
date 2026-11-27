-- Alertas que abre y cierra el Edge al evaluar los umbrales de cada sala.
-- room_id no lleva FK: la sala vive en otro contexto y se valida por su fachada.
CREATE TABLE alerting.alert (
    id                UUID             PRIMARY KEY,
    source_key        VARCHAR(120)     NOT NULL UNIQUE,
    room_id           UUID             NOT NULL,
    rule              VARCHAR(40)      NOT NULL,
    severity          VARCHAR(16)      NOT NULL,
    message           VARCHAR(200)     NOT NULL,
    value             DOUBLE PRECISION,
    status            VARCHAR(16)      NOT NULL,
    opened_at         TIMESTAMPTZ      NOT NULL,
    closed_at         TIMESTAMPTZ,
    closed_by         VARCHAR(8),
    acknowledged_at   TIMESTAMPTZ,
    acknowledged_by   UUID,
    corrective_action VARCHAR(500)
);

CREATE INDEX ix_alert_room_opened ON alerting.alert (room_id, opened_at DESC);
CREATE INDEX ix_alert_status ON alerting.alert (status, opened_at DESC);
