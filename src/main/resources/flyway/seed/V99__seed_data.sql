-- This script is for seeding the database with initial data for local development.
-- It is executed by the db-seeder service in docker-compose after Flyway migrations have run.

-- Sensor 1: Temperature and Humidity Sensor at Planet Express HQ (New New York)
DO $$
DECLARE
    -- Declare variables to hold IDs
v_sensor_id TEXT := 'PLANET-EXPRESS-HQ-TEMP-HUMIDITY';
    v_event_id_1 BIGINT;
    v_event_id_2 BIGINT;
    v_sensor_type_id INT;
    v_state_id INT;
    v_measurement_event_type_id INT;
    v_temp_metric_id INT;
    v_humidity_metric_id INT;
    v_system_user_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'; -- Static UUID for seed data
BEGIN
    -- Get lookup IDs from existing tables
SELECT sensor_type_id INTO v_sensor_type_id FROM sensor_types WHERE type_name = 'TEMPERATURE_AND_HUMIDITY_SENSOR';
SELECT state_id INTO v_state_id FROM sensor_states WHERE state_name = 'CONNECTED';
SELECT event_type_id INTO v_measurement_event_type_id FROM audit_event_types WHERE type_name = 'MEASUREMENT';
SELECT metric_type_id INTO v_temp_metric_id FROM metric_types WHERE type_name = 'TEMPERATURE';
SELECT metric_type_id INTO v_humidity_metric_id FROM metric_types WHERE type_name = 'HUMIDITY';

-- Insert Sensor (Planet Express HQ -> Times Square, NYC)
INSERT INTO sensors (sensor_id, sensor_type_id, location, latitude, longitude, state_id, description, last_updated, last_updated_by)
VALUES (v_sensor_id, v_sensor_type_id, 'New New York City', 40.758000, -73.985500, v_state_id, 'Monitors climate control in the main hangar.', NOW(), v_system_user_id)
    ON CONFLICT (sensor_id) DO NOTHING;

-- Insert Event 1 (15 minutes ago)
INSERT INTO sensor_audit_events (sensor_id, event_type_id, event_timestamp)
VALUES (v_sensor_id, v_measurement_event_type_id, NOW() - INTERVAL '15 minutes')
    RETURNING event_id INTO v_event_id_1;

-- Insert Measurements for Event 1
INSERT INTO sensor_measurements (event_id, metric_type_id, measurement_value)
VALUES (v_event_id_1, v_temp_metric_id, 22.5),  -- 22.5 C
       (v_event_id_1, v_humidity_metric_id, 45.2); -- 45.2 %

-- Insert Event 2 (3 minutes ago)
INSERT INTO sensor_audit_events (sensor_id, event_type_id, event_timestamp)
VALUES (v_sensor_id, v_measurement_event_type_id, NOW() - INTERVAL '3 minutes')
    RETURNING event_id INTO v_event_id_2;

-- Insert Measurements for Event 2
INSERT INTO sensor_measurements (event_id, metric_type_id, measurement_value)
VALUES (v_event_id_2, v_temp_metric_id, 22.6),  -- 22.6 C
       (v_event_id_2, v_humidity_metric_id, 45.0); -- 45.0 %

END $$;


-- Sensor 2: Noise Sensor at Mom's Friendly Robot Company (New New York)
DO $$
DECLARE
    -- Declare variables
v_sensor_id TEXT := 'MOMCORP-FACTORY-NOISE';
    v_event_id_1 BIGINT;
    v_sensor_type_id INT;
    v_state_id INT;
    v_measurement_event_type_id INT;
    v_noise_metric_id INT;
    v_system_user_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'; -- Static UUID for seed data
BEGIN
    -- Get lookup IDs
SELECT sensor_type_id INTO v_sensor_type_id FROM sensor_types WHERE type_name = 'NOISE_SENSOR';
SELECT state_id INTO v_state_id FROM sensor_states WHERE state_name = 'CONNECTED';
SELECT event_type_id INTO v_measurement_event_type_id FROM audit_event_types WHERE type_name = 'MEASUREMENT';
SELECT metric_type_id INTO v_noise_metric_id FROM metric_types WHERE type_name = 'NOISE_LEVEL';

-- Insert Sensor (MomCorp HQ -> Wall Street, NYC)
INSERT INTO sensors (sensor_id, sensor_type_id, location, latitude, longitude, state_id, description, last_updated, last_updated_by)
VALUES (v_sensor_id, v_sensor_type_id, 'Financial District, New New York', 40.706100, -74.008800, v_state_id, 'Measures decibel levels on the robot factory floor.', NOW(), v_system_user_id)
    ON CONFLICT (sensor_id) DO NOTHING;

-- Insert Event
INSERT INTO sensor_audit_events (sensor_id, event_type_id, event_timestamp)
VALUES (v_sensor_id, v_measurement_event_type_id, NOW() - INTERVAL '2 minutes')
    RETURNING event_id INTO v_event_id_1;

-- Insert Measurement
INSERT INTO sensor_measurements (event_id, metric_type_id, measurement_value)
VALUES (v_event_id_1, v_noise_metric_id, 92.4); -- 92.4 dB (Loud!)

END $$;

-- Sensor 3: UV Sensor at The Head Museum (Washington D.C.)
DO $$
DECLARE
    -- Declare variables
v_sensor_id TEXT := 'HEAD-MUSEUM-LAWN-UV';
    v_sensor_type_id INT;
    v_state_id INT;
    v_system_user_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'; -- Static UUID for seed data
BEGIN
    -- Get lookup IDs
SELECT sensor_type_id INTO v_sensor_type_id FROM sensor_types WHERE type_name = 'UV_SENSOR';
SELECT state_id INTO v_state_id FROM sensor_states WHERE state_name = 'DEACTIVATED'; -- Note: This one is off

-- Insert Sensor (Head Museum -> National Mall, D.C.)
INSERT INTO sensors (sensor_id, sensor_type_id, location, latitude, longitude, state_id, description, last_updated, last_updated_by)
VALUES (v_sensor_id, v_sensor_type_id, 'Washington D.C.', 38.891300, -77.026000, v_state_id, 'Monitors UV to protect the preserved heads during outdoor events. Currently deactivated for maintenance.', NOW(), v_system_user_id)
    ON CONFLICT (sensor_id) DO NOTHING;

-- Note: No events or measurements are added for this sensor since it is deactivated.

END $$;

-- Sensor 4: Rain Gauge at The Head Museum (Washington D.C.)
DO $$
DECLARE
    -- Declare variables
v_sensor_id TEXT := 'HEAD-MUSEUM-ROOF-RAIN-GAUGE';
    v_sensor_type_id INT;
    v_state_id INT;
    v_offline_event_type_id INT;
    v_system_user_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'; -- Static UUID for seed data
BEGIN
    -- Get lookup IDs
SELECT sensor_type_id INTO v_sensor_type_id FROM sensor_types WHERE type_name = 'RAIN_GAUGE';
SELECT state_id INTO v_state_id FROM sensor_states WHERE state_name = 'DISCONNECTED';
SELECT event_type_id INTO v_offline_event_type_id FROM audit_event_types WHERE type_name = 'OFFLINE';

-- Insert Sensor (Head Museum -> National Mall, D.C.)
INSERT INTO sensors (sensor_id, sensor_type_id, location, latitude, longitude, state_id, description, last_updated, last_updated_by)
VALUES (v_sensor_id, v_sensor_type_id, 'Washington D.C.', 38.891300, -77.026000, v_state_id, 'Measures rainfall on the roof of the Head Museum. Currently has a network connectivity issue.', NOW(), v_system_user_id)
    ON CONFLICT (sensor_id) DO NOTHING;

-- Insert an OFFLINE event that happened 5 minutes ago to log the state change
INSERT INTO sensor_audit_events (sensor_id, event_type_id, event_timestamp)
VALUES (v_sensor_id, v_offline_event_type_id, NOW() - INTERVAL '5 minutes');

END $$;

-- Sensor 5: Smell-O-Scope at Planet Express HQ
DO $$
DECLARE
v_sensor_id TEXT := 'PLANET-EXPRESS-SMELL-O-SCOPE';
    v_event_id BIGINT;
    v_sensor_type_id INT;
    v_state_id INT;
    v_measurement_event_type_id INT;
    v_funk_metric_id INT;
    v_system_user_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
BEGIN
SELECT sensor_type_id INTO v_sensor_type_id FROM sensor_types WHERE type_name = 'SMELL_O_SCOPE';
SELECT state_id INTO v_state_id FROM sensor_states WHERE state_name = 'CONNECTED';
SELECT event_type_id INTO v_measurement_event_type_id FROM audit_event_types WHERE type_name = 'MEASUREMENT';
SELECT metric_type_id INTO v_funk_metric_id FROM metric_types WHERE type_name = 'FUNK_LEVEL';

INSERT INTO sensors (sensor_id, sensor_type_id, location, latitude, longitude, state_id, description, last_updated, last_updated_by)
VALUES (v_sensor_id, v_sensor_type_id, 'Planet Express HQ', 40.758000, -73.985500, v_state_id, 'Measures interstellar odors.', NOW(), v_system_user_id)
    ON CONFLICT (sensor_id) DO NOTHING;

INSERT INTO sensor_audit_events (sensor_id, event_type_id, event_timestamp)
VALUES (v_sensor_id, v_measurement_event_type_id, NOW() - INTERVAL '10 minutes')
    RETURNING event_id INTO v_event_id;

INSERT INTO sensor_measurements (event_id, metric_type_id, measurement_value)
VALUES (v_event_id, v_funk_metric_id, 8.7); -- A very funky reading

END $$;