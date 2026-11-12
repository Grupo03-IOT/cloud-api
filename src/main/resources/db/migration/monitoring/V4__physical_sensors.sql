ALTER TABLE monitoring.room_reading
    ADD COLUMN sensor_sampled_at TIMESTAMPTZ,
    ADD COLUMN sensor_movement BOOLEAN,
    ADD COLUMN sensor_distance_cm REAL,
    ADD COLUMN sensor_potentiometer INTEGER,
    ADD COLUMN sensor_button1 BOOLEAN,
    ADD COLUMN sensor_button2 BOOLEAN,
    ADD COLUMN sensor_button3 BOOLEAN;
