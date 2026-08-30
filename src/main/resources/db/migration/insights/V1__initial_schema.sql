CREATE SCHEMA IF NOT EXISTS insights;

CREATE TABLE insights.weather_observation (
    id          UUID        PRIMARY KEY,
    observed_at TIMESTAMPTZ NOT NULL,
    temp_c      REAL,
    rh_pct      REAL,
    condition   VARCHAR(64),
    fetched_at  TIMESTAMPTZ NOT NULL
);
