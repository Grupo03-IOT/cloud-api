-- Reportes de incomodidad de los miembros, con la lectura de la sala en ese momento.
-- user_id no lleva FK: el usuario vive en el contexto iam.
CREATE TABLE monitoring.discomfort_report (
    id           UUID         PRIMARY KEY,
    room_id      UUID         NOT NULL REFERENCES monitoring.room (id),
    user_id      UUID         NOT NULL,
    category     VARCHAR(8)   NOT NULL,
    comment      VARCHAR(500),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    reading_ts   TIMESTAMPTZ,
    laeq         REAL,
    temp_c       REAL,
    rh_pct       REAL,
    occupied_pct REAL
);

CREATE INDEX ix_discomfort_room_created ON monitoring.discomfort_report (room_id, created_at DESC);
CREATE INDEX ix_discomfort_user_created ON monitoring.discomfort_report (user_id, created_at DESC);
