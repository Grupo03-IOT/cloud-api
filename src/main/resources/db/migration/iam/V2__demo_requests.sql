-- US06: solicitudes de demostración enviadas desde el Landing Page.
CREATE TABLE iam.demo_request (
    id           UUID         PRIMARY KEY,
    full_name    VARCHAR(100) NOT NULL,
    work_email   VARCHAR(150) NOT NULL,
    company      VARCHAR(150) NOT NULL,
    room_count   INTEGER      NOT NULL CHECK (room_count BETWEEN 1 AND 10000),
    consented_at TIMESTAMPTZ  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_demo_request_created_at ON iam.demo_request (created_at DESC);
