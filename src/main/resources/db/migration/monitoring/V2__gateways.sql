CREATE TABLE monitoring.gateway (
    id              UUID        PRIMARY KEY,
    code            VARCHAR(64) NOT NULL,
    site_id         UUID        NOT NULL REFERENCES monitoring.site (id),
    version         VARCHAR(32),
    last_seen       TIMESTAMPTZ NOT NULL,
    pending_upload  INTEGER     NOT NULL DEFAULT 0,
    rooms_reported  INTEGER     NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    deleted_at      TIMESTAMPTZ,
    created_by      UUID,
    updated_by      UUID
);
