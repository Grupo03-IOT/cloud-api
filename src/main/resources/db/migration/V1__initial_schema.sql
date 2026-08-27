CREATE TABLE site (
    id          UUID         PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    address     VARCHAR(256),
    timezone    VARCHAR(64)  NOT NULL DEFAULT 'America/Lima',
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    deleted_at  TIMESTAMPTZ,
    created_by  UUID,
    updated_by  UUID
);

CREATE TABLE room_type (
    id           UUID         PRIMARY KEY,
    site_id      UUID         NOT NULL REFERENCES site (id),
    code         VARCHAR(32)  NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    description  VARCHAR(256),
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    deleted_at   TIMESTAMPTZ,
    created_by   UUID,
    updated_by   UUID
);

CREATE TABLE threshold (
    id                UUID        PRIMARY KEY,
    room_type_id      UUID        NOT NULL REFERENCES room_type (id),
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

CREATE TABLE room (
    id           UUID         PRIMARY KEY,
    site_id      UUID         NOT NULL REFERENCES site (id),
    room_type_id UUID         REFERENCES room_type (id),
    code         VARCHAR(64)  NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    floor        VARCHAR(32),
    capacity     INTEGER,
    area_m2      REAL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    deleted_at   TIMESTAMPTZ,
    created_by   UUID,
    updated_by   UUID
);

CREATE TABLE device (
    id           UUID        PRIMARY KEY,
    room_id      UUID        REFERENCES room (id),
    code         VARCHAR(64) NOT NULL,
    fw_version   VARCHAR(32),
    last_seen    TIMESTAMPTZ,
    last_seq     BIGINT      NOT NULL DEFAULT -1,
    lost_batches BIGINT      NOT NULL DEFAULT 0,
    status       VARCHAR(16) NOT NULL DEFAULT 'unknown',
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    deleted_at   TIMESTAMPTZ,
    created_by   UUID,
    updated_by   UUID
);

CREATE TABLE room_reading (
    id              UUID        PRIMARY KEY,
    room_id         UUID        NOT NULL REFERENCES room (id),
    ts              TIMESTAMPTZ NOT NULL,
    period_s        INTEGER     NOT NULL DEFAULT 60,
    laeq            REAL,
    l10             REAL,
    l50             REAL,
    l90             REAL,
    lmax            REAL,
    lmin            REAL,
    temp_c          REAL,
    rh_pct          REAL,
    pmv             REAL,
    ppd             REAL,
    thermal_verdict VARCHAR(24),
    occupied_pct    REAL        NOT NULL DEFAULT 0,
    transitions     INTEGER     NOT NULL DEFAULT 0,
    batches         INTEGER,
    expected        INTEGER,
    received_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
