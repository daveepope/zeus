package org.zeus.service.sensorLifecycle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.zeus.dbo.Sensor;
import org.zeus.dbo.SensorType;
import org.zeus.domain.event.SensorLifecycleEvent;
import org.zeus.exception.SensorAlreadyExistsException;
import org.zeus.exception.SensorNotFoundException;
import org.zeus.mapper.sensor.SensorMapper;
import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;
import org.zeus.model.SensorState;
import org.zeus.repository.sensor.SensorRepository;
import org.zeus.repository.sensor.SensorStateRepository;
import org.zeus.repository.sensor.SensorTypeRepository;
import org.zeus.repository.telemetry.AuditEventRepository;
import org.zeus.repository.telemetry.AuditEventTypeRepository;
import org.zeus.repository.telemetry.SupportedMetricsRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.zeus.domain.event.DomainConstants.SENSOR_LIFECYCLE_TOPIC;

@ExtendWith(MockitoExtension.class)
class SensorLifecycleOrchestratorImplTest {

    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private AuditEventTypeRepository auditEventTypeRepository;
    @Mock
    private SensorTypeRepository sensorTypeRepository;
    @Mock
    private SensorStateRepository sensorStateRepository;
    @Mock
    private SupportedMetricsRepository supportedMetricsRepository;
    @Mock
    private AuditEventRepository auditEventRepository;
    @Mock
    private KafkaTemplate<String, SensorLifecycleEvent> kafkaTemplate;
    @Mock
    private SensorMapper sensorMapper;

    @InjectMocks
    private SensorLifecycleOrchestratorImpl sensorLifecycleOrchestrator;

    private Sensor sensorDbo;
    private SensorResponse sensorResponse;
    private org.zeus.dbo.SensorState disconnectedStateDbo;

    @BeforeEach
    void setUp() {
        sensorDbo = new Sensor();
        sensorDbo.setSensorId("sensor-123");

        SensorType sensorTypeDbo = new SensorType();
        sensorTypeDbo.setSensorTypeId(1);
        sensorDbo.setSensorType(sensorTypeDbo);

        sensorResponse = new SensorResponse();
        sensorResponse.setSensorId("sensor-123");

        disconnectedStateDbo = new org.zeus.dbo.SensorState();
        disconnectedStateDbo.setStateName(SensorState.DISCONNECTED.getValue());
    }

    @Test
    void registerSensor_whenSuccessful_shouldSaveAndReturnSensor() {
        SensorRegistrationRequest request = new SensorRegistrationRequest();
        request.setSensorId("new-sensor");
        request.setSensorType(org.zeus.model.SensorType.NOISE_SENSOR);

        when(sensorRepository.existsById("new-sensor")).thenReturn(false);
        when(sensorTypeRepository.findByTypeName(anyString())).thenReturn(Optional.of(new SensorType()));
        when(sensorStateRepository.findByStateName(anyString())).thenReturn(Optional.of(disconnectedStateDbo));
        when(sensorMapper.toEntity(any(), any(), any())).thenReturn(sensorDbo);
        when(sensorRepository.save(any(Sensor.class))).thenReturn(sensorDbo);
        when(auditEventTypeRepository.findByTypeName(anyString())).thenReturn(Optional.of(new org.zeus.dbo.AuditEventType()));
        when(sensorMapper.toResponse(any(Sensor.class))).thenReturn(sensorResponse);

        SensorResponse result = sensorLifecycleOrchestrator.registerSensor(request);

        assertThat(result).isNotNull();
        assertThat(result.getSensorId()).isEqualTo("sensor-123");
        verify(sensorRepository).save(any(Sensor.class));
        verify(auditEventRepository).save(any());
        verify(kafkaTemplate).send(eq(SENSOR_LIFECYCLE_TOPIC), eq("sensor-123"), any(SensorLifecycleEvent.class));
    }

    @Test
    void registerSensor_whenSensorAlreadyExists_throwsSensorAlreadyExistsException() {
        SensorRegistrationRequest request = new SensorRegistrationRequest();
        request.setSensorId("existing-sensor");
        when(sensorRepository.existsById("existing-sensor")).thenReturn(true);

        assertThrows(SensorAlreadyExistsException.class, () -> {
            sensorLifecycleOrchestrator.registerSensor(request);
        });

        verify(sensorRepository, never()).save(any());
    }

    @Test
    void getSensorById_whenSensorExists_shouldReturnSensor() {
        when(sensorRepository.findById("sensor-123")).thenReturn(Optional.of(sensorDbo));
        when(sensorMapper.toResponse(sensorDbo)).thenReturn(sensorResponse);

        SensorResponse result = sensorLifecycleOrchestrator.getSensorById("sensor-123");

        assertThat(result).isNotNull();
        assertThat(result.getSensorId()).isEqualTo("sensor-123");
        verify(supportedMetricsRepository).findById_SensorTypeId(any());
    }

    @Test
    void getSensorById_whenSensorNotFound_throwsSensorNotFoundException() {
        when(sensorRepository.findById("unknown-sensor")).thenReturn(Optional.empty());

        assertThrows(SensorNotFoundException.class, () -> {
            sensorLifecycleOrchestrator.getSensorById("unknown-sensor");
        });
    }

    @Test
    void getSensors_whenSensorsExist_shouldReturnListOfSensors() {
        when(sensorRepository.findAll()).thenReturn(List.of(sensorDbo));
        when(sensorMapper.toResponse(sensorDbo)).thenReturn(sensorResponse);

        List<SensorResponse> results = sensorLifecycleOrchestrator.getSensors();

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSensorId()).isEqualTo("sensor-123");
    }

    @Test
    void processHeartbeat_whenSensorWasDisconnected_updatesStateAndPublishesEvent() {
        sensorDbo.setState(disconnectedStateDbo);
        org.zeus.dbo.SensorState connectedStateDbo = new org.zeus.dbo.SensorState();
        connectedStateDbo.setStateName(SensorState.CONNECTED.getValue());

        when(sensorRepository.findById("sensor-123")).thenReturn(Optional.of(sensorDbo));
        when(sensorStateRepository.findByStateName(SensorState.CONNECTED.getValue())).thenReturn(Optional.of(connectedStateDbo));

        sensorLifecycleOrchestrator.processHeartbeat("sensor-123", SensorState.CONNECTED);

        ArgumentCaptor<Sensor> sensorCaptor = ArgumentCaptor.forClass(Sensor.class);
        verify(sensorRepository).save(sensorCaptor.capture());
        Sensor savedSensor = sensorCaptor.getValue();
        assertThat(savedSensor.getState().getStateName()).isEqualTo(SensorState.CONNECTED.getValue());

        ArgumentCaptor<SensorLifecycleEvent> eventCaptor = ArgumentCaptor.forClass(SensorLifecycleEvent.class);
        verify(kafkaTemplate).send(eq(SENSOR_LIFECYCLE_TOPIC), eq("sensor-123"), eventCaptor.capture());
        SensorLifecycleEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getStatus()).isEqualTo(SensorState.CONNECTED.getValue());
    }

    @Test
    void processHeartbeat_whenSensorIsAlreadyConnected_updatesStateWithoutPublishingEvent() {
        org.zeus.dbo.SensorState connectedStateDbo = new org.zeus.dbo.SensorState();
        connectedStateDbo.setStateName(SensorState.CONNECTED.getValue());
        sensorDbo.setState(connectedStateDbo);

        when(sensorRepository.findById("sensor-123")).thenReturn(Optional.of(sensorDbo));
        when(sensorStateRepository.findByStateName(SensorState.CONNECTED.getValue())).thenReturn(Optional.of(connectedStateDbo));

        sensorLifecycleOrchestrator.processHeartbeat("sensor-123", SensorState.CONNECTED);

        verify(sensorRepository).save(any(Sensor.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
}