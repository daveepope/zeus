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
import org.zeus.model.MetricType;
import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;
import org.zeus.model.SensorState;
import org.zeus.repository.measurement.MetricTypeRepository;
import org.zeus.repository.sensor.SensorRepository;
import org.zeus.repository.sensor.SensorStateRepository;
import org.zeus.repository.sensor.SensorTypeRepository;
import org.zeus.repository.measurement.SupportedMetricsRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SensorLifecycleOrchestratorImpl implements SensorLifecycleOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SensorLifecycleOrchestratorImpl.class);
    private static final String INITIAL_SENSOR_STATE = SensorState.DISCONNECTED.toString();

    private final SensorRepository sensorRepository;
    private final SensorTypeRepository sensorTypeRepository;
    private final SensorStateRepository sensorStateRepository;
    private final SupportedMetricsRepository supportedMetricsRepository;
    private final MetricTypeRepository metricTypeRepository;

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

        var dbSensorToSave = sensorMapper.toEntity(request, sensorType, initialState);

        dbSensorToSave.setLastUpdated(OffsetDateTime.now(ZoneOffset.UTC));
        dbSensorToSave.setLastUpdatedBy(UUID.randomUUID());
        dbSensorToSave.setRegistrationDate(OffsetDateTime.now(ZoneOffset.UTC));

        var savedSensor = sensorRepository.save(dbSensorToSave);
        log.info("Successfully saved sensor with ID: {}", savedSensor.getSensorId());

        var apiSensor = sensorMapper.toResponse(savedSensor);
        apiSensor.setSupportedMetrics(sensorMapper.mapMetricTypes(supportedMetricsRepository.findById_SensorTypeId(sensorType.getSensorTypeId())));
        return apiSensor;
    }

    @Override
    public SensorResponse getSensorById(String sensorId) {
        log.info("Attempting to retrieve sensor with ID: {}", sensorId);
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new SensorNotFoundException("Sensor with ID '" + sensorId + "' not found."));
        var apiSensor =  sensorMapper.toResponse(sensor);
        apiSensor.setSupportedMetrics(sensorMapper.mapMetricTypes(supportedMetricsRepository.findById_SensorTypeId(sensor.getSensorType().getSensorTypeId())));
        log.info("Successfully retrieved sensor with ID: {}", sensorId);
        return apiSensor;
    }

    @Override
    public List<org.zeus.model.SensorType> getSupportedSensorTypes() {
        var supportedDbTypes = sensorTypeRepository.findAll();
        return sensorMapper.mapToApiSensorTypes(supportedDbTypes);
    }

    @Override
    public List<SensorResponse> getSensors() {
        var dbSensors =  sensorRepository.findAll();
        var apiSensors = new ArrayList<SensorResponse>();

        for (var dbSensor : dbSensors) {
            var apiSensor =  sensorMapper.toResponse(dbSensor);
            apiSensor.setSupportedMetrics(sensorMapper.mapMetricTypes(supportedMetricsRepository.findById_SensorTypeId(dbSensor.getSensorType().getSensorTypeId())));
            apiSensors.add(apiSensor);
        }
        return apiSensors;
    }
}