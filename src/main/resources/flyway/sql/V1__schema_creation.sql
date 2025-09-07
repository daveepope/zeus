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

CREATE TABLE audit_event_types
(
    event_type_id SERIAL PRIMARY KEY,
    type_name     TEXT NOT NULL UNIQUE
);

CREATE TABLE supported_metrics
(
    sensor_type_id INTEGER NOT NULL REFERENCES sensor_types (sensor_type_id) ON DELETE CASCADE,
    metric_type_id INTEGER NOT NULL REFERENCES metric_types (metric_type_id) ON DELETE CASCADE,
    PRIMARY KEY (sensor_type_id, metric_type_id)
);

CREATE TABLE sensors
(
    sensor_id         TEXT PRIMARY KEY,
    sensor_type_id    INTEGER NOT NULL REFERENCES sensor_types (sensor_type_id) ON DELETE RESTRICT,
    location          TEXT NOT NULL,
    latitude          NUMERIC(9, 6) NOT NULL CHECK (latitude >= -90 AND latitude <= 90),
    longitude         NUMERIC(9, 6) NOT NULL CHECK (longitude >= -180 AND longitude <= 180),
    state_id          INTEGER NOT NULL REFERENCES sensor_states (state_id) ON DELETE RESTRICT,
    registration_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_by   UUID NOT NULL,
    description       TEXT
);

CREATE TABLE sensor_audit_events
(
    event_id        BIGSERIAL PRIMARY KEY,
    sensor_id       TEXT NOT NULL REFERENCES sensors (sensor_id) ON DELETE CASCADE,
    event_type_id   INTEGER NOT NULL REFERENCES audit_event_types (event_type_id) ON DELETE RESTRICT,
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sensor_measurements
(
    measurement_id     BIGSERIAL PRIMARY KEY,
    event_id           BIGINT NOT NULL REFERENCES sensor_audit_events (event_id) ON DELETE CASCADE,
    metric_type_id     INTEGER NOT NULL REFERENCES metric_types (metric_type_id) ON DELETE RESTRICT,
    measurement_value  NUMERIC(15, 6) NOT NULL,
    UNIQUE (event_id, metric_type_id)
);

CREATE INDEX idx_sensor_events_sensor_id_timestamp ON sensor_audit_events (sensor_id, event_timestamp);

CREATE INDEX idx_sensor_measurements_event_id ON sensor_measurements (event_id);

-- Initial data for lookup tables
INSERT INTO sensor_types (type_name, description)
VALUES
    ('TEMPERATURE_AND_HUMIDITY_SENSOR', 'Measures air temperature and relative humidity.'),
    ('WIND_SPEED_SENSOR', 'Measures wind speed.'),
    ('WIND_DIRECTION_SENSOR', 'Measures wind direction.'),
    ('RAIN_GAUGE', 'Measures rainfall.'),
    ('ATMOSPHERIC_PRESSURE_SENSOR', 'Measures atmospheric pressure.'),
    ('SOLAR_RADIATION_SENSOR', 'Measures total solar energy.'),
    ('SUNLIGHT_SENSOR', 'Measures sunlight intensity.'),
    ('UV_SENSOR', 'Measures UV radiation.'),
    ('NOISE_SENSOR', 'Measures sound levels.'),
    ('RAIN_AND_SNOW_SENSOR', 'Detects precipitation and its type (wet or frozen).'),
    ('SMELL_O_SCOPE', 'Professor Farnsworth''s classic device for detecting and locating interstellar smells. The degree of odour can be measured on a meter called the "Funkometer"');

INSERT INTO metric_types (type_name, unit_symbol)
VALUES
    ('TEMPERATURE', 'C'),
    ('HUMIDITY', '%'),
    ('WIND_SPEED', 'm/s'),
    ('WIND_DIRECTION', 'degrees'),
    ('RAINFALL', 'mm'),
    ('ATMOSPHERIC_PRESSURE', 'hPa'),
    ('SOLAR_RADIATION', 'W/m²'),
    ('SUNLIGHT_INTENSITY', 'lux'),
    ('UV_RADIATION', 'nm'),
    ('NOISE_LEVEL', 'dB'),
    ('FUNK_LEVEL', 'funk');

INSERT INTO sensor_states (state_name)
VALUES
    ('CONNECTED'),
    ('DISCONNECTED'),
    ('DEACTIVATED'),
    ('OFFLINE'),
    ('ERROR'),
    ('MAINTENANCE'),
    ('LOW_BATTERY');

INSERT INTO audit_event_types (type_name)
VALUES
    ('OFFLINE'),
    ('CONNECTED'),
    ('DISCONNECTED'),
    ('MEASUREMENT'),
    ('ERROR'),
    ('MAINTENANCE'),
    ('LOW_BATTERY');

-- Populating the supported_metrics table with sample relationships
INSERT INTO supported_metrics (sensor_type_id, metric_type_id)
VALUES
    (1, 1),  -- Temperature and Humidity Sensor -> temperature
    (1, 2),  -- Temperature and Humidity Sensor -> humidity
    (2, 3),  -- Wind Speed Sensor -> wind speed
    (3, 4),  -- Wind Direction Sensor -> wind direction
    (4, 5),  -- Rain Gauge -> rainfall
    (5, 6),  -- Atmospheric Pressure Sensor -> atmospheric pressure
    (6, 7),  -- Solar Radiation Sensor -> solar radiation
    (7, 8),  -- Sunlight Sensor -> sunlight intensity
    (8, 9),  -- UV Sensor -> uv radiation
    (9, 10), -- Noise Sensor -> noise level
    (10, 5), -- Rain and Snow Sensor -> rainfall
    (11, 3), -- Smell-O-Scope -> wind speed
    (11, 4), -- Smell-O-Scope -> wind direction
    (11, 11);-- Smell-O-Scope -> funk level
