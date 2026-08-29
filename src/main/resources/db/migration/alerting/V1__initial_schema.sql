CREATE SCHEMA IF NOT EXISTS alerting;

CREATE TABLE alerting.threshold (
    id                UUID        PRIMARY KEY,
    room_type_id      UUID        NOT NULL,
    metric            VARCHAR(24) NOT NULL,
    warn_value        REAL        NOT NULL,
    critical_value    REAL,
    sustained_minutes INTEGER     NOT NULL DEFAULT 2,
    enabled           BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    deleted_at        TIMESTAMPTZ,
    created_by        UUID,
    updated_by        UUID
);
