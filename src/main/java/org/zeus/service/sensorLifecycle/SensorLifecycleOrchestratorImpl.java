package org.zeus.service.sensorLifecycle;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.dbo.Sensor;
import org.zeus.dbo.SensorType;
import org.zeus.exception.SensorAlreadyExistsException;
import org.zeus.exception.SensorNotFoundException;
import org.zeus.mapper.sensor.SensorMapper;
import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;
import org.zeus.model.SensorState;
import org.zeus.repository.sensor.SensorRepository;
import org.zeus.repository.sensor.SensorStateRepository;
import org.zeus.repository.sensor.SensorTypeRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SensorLifecycleOrchestratorImpl implements SensorLifecycleOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SensorLifecycleOrchestratorImpl.class);
    private static final String INITIAL_SENSOR_STATE = SensorState.DISCONNECTED.toString();

    private final SensorRepository sensorRepository;
    private final SensorTypeRepository sensorTypeRepository;
    private final SensorStateRepository sensorStateRepository;
    private final SensorMapper sensorMapper;

    @Override
    @Transactional
    public SensorResponse registerSensor(SensorRegistrationRequest request) {
        log.info("Attempting to register new sensor with ID: {}", request.getSensorId());

        if (sensorRepository.existsById(request.getSensorId())) {
            throw new SensorAlreadyExistsException("Sensor with ID '" + request.getSensorId() + "' already exists.");
        }

        String sensorTypeName = request.getSensorType().getValue();
        SensorType sensorType = sensorTypeRepository.findByTypeName(sensorTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Sensor Type: '" + sensorTypeName + "'"));

        org.zeus.dbo.SensorState initialState = sensorStateRepository.findByStateName(INITIAL_SENSOR_STATE)
                .orElseThrow(() -> new IllegalStateException("Initial state '" + INITIAL_SENSOR_STATE + "' not found in database. Please check initial data seed."));

        Sensor newSensor = sensorMapper.toEntity(request, sensorType, initialState);

        newSensor.setLastUpdated(OffsetDateTime.now(ZoneOffset.UTC));
        newSensor.setLastUpdatedBy(UUID.randomUUID());
        newSensor.setRegistrationDate(OffsetDateTime.now(ZoneOffset.UTC));

        Sensor savedSensor = sensorRepository.save(newSensor);
        log.info("Successfully saved sensor with ID: {}", savedSensor.getSensorId());

        return sensorMapper.toResponse(savedSensor);
    }

    @Override
    public SensorResponse getSensorById(String sensorId) {
        log.info("Attempting to retrieve sensor with ID: {}", sensorId);
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new SensorNotFoundException("Sensor with ID '" + sensorId + "' not found."));

        log.info("Successfully retrieved sensor with ID: {}", sensorId);
        return sensorMapper.toResponse(sensor);
    }
}