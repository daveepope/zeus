CREATE TABLE sensor_types
(
    sensor_type_id SERIAL PRIMARY KEY,
    type_name      TEXT NOT NULL UNIQUE,
    description    TEXT
);

CREATE TABLE metric_types
(
    metric_type_id SERIAL PRIMARY KEY,
    type_name      TEXT NOT NULL UNIQUE,
    unit_symbol    TEXT NOT NULL
);

CREATE TABLE sensor_states
(
    state_id   SERIAL PRIMARY KEY,
    state_name TEXT NOT NULL UNIQUE
);

CREATE TABLE sensor_event_types
(
    event_type_id SERIAL PRIMARY KEY,
    type_name     TEXT NOT NULL UNIQUE
);

CREATE TABLE sensors
(
    sensor_id         TEXT PRIMARY KEY,
    sensor_type_id    INTEGER REFERENCES sensor_types (sensor_type_id) ON DELETE RESTRICT,
    location          TEXT,
    latitude          NUMERIC(9, 6) CHECK (latitude >= -90 AND latitude <= 90),
    longitude         NUMERIC(9, 6) CHECK (longitude >= -180 AND longitude <= 180),
    state_id          INTEGER REFERENCES sensor_states (state_id) ON DELETE RESTRICT,
    registration_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    description       TEXT
);

CREATE TABLE sensor_events
(
    event_id        BIGSERIAL PRIMARY KEY,
    sensor_id       TEXT REFERENCES sensors (sensor_id) ON DELETE CASCADE,
    event_type_id   INTEGER REFERENCES sensor_event_types (event_type_id) ON DELETE RESTRICT,
    event_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sensor_measurements
(
    measurement_id     BIGSERIAL PRIMARY KEY,
    event_id           BIGINT REFERENCES sensor_events (event_id) ON DELETE CASCADE,
    metric_type_id     INTEGER REFERENCES metric_types (metric_type_id) ON DELETE RESTRICT,
    measurement_value  NUMERIC(15, 6) NOT NULL,
    UNIQUE (event_id, metric_type_id)
);

CREATE INDEX idx_sensor_events_sensor_id_timestamp ON sensor_events (sensor_id, event_timestamp);

CREATE INDEX idx_sensor_measurements_event_id ON sensor_measurements (event_id);

INSERT INTO sensor_types (type_name, description)
VALUES ('Temperature and Humidity Sensor', 'Measures air temperature and relative humidity.'),
       ('Wind Speed Sensor', 'Measures wind speed.'),
       ('Wind Direction Sensor', 'Measures wind direction.'),
       ('Rain Gauge', 'Measures rainfall.'),
       ('Atmospheric Pressure Sensor', 'Measures atmospheric pressure.'),
       ('Solar Radiation Sensor', 'Measures total solar energy.'),
       ('Sunlight Sensor', 'Measures sunlight intensity.'),
       ('UV Sensor', 'Measures UV radiation.'),
       ('Noise Sensor', 'Measures sound levels.'),
       ('Rain and Snow Sensor', 'Detects precipitation and its type (wet or frozen).');

INSERT INTO metric_types (type_name, unit_symbol)
VALUES ('temperature', 'C'),
       ('humidity', '%'),
       ('wind speed', 'm/s'),
       ('wind direction', 'degrees'),
       ('rainfall', 'mm'),
       ('atmospheric pressure', 'hPa'),
       ('solar radiation', 'W/m²'),
       ('sunlight intensity', 'lux'),
       ('uv radiation', 'nm'),
       ('noise level', 'dB');

INSERT INTO sensor_states (state_name)
VALUES ('CONNECTED'),
       ('DISCONNECTED'),
       ('DEACTIVATED');

INSERT INTO sensor_event_types (type_name)
VALUES ('MEASUREMENT'),
       ('CONNECTED'),
       ('OFFLINE');

