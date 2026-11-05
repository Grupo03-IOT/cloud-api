ALTER TABLE monitoring.room_reading
    ADD COLUMN forecast_probability REAL,
    ADD COLUMN forecast_horizon_min INTEGER,
    ADD COLUMN forecast_temperature_limit_c REAL,
    ADD COLUMN forecast_model_version VARCHAR(64),
    ADD COLUMN forecast_generated_at TIMESTAMPTZ;
