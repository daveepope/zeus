package org.zeus.service.sensorLifecycle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.zeus.dbo.Sensor;
import org.zeus.domain.event.SensorLifecycleEvent;
import org.zeus.model.SensorState;
import org.zeus.repository.sensor.SensorRepository;
import org.zeus.repository.sensor.SensorStateRepository;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.zeus.domain.event.DomainConstants.SENSOR_LIFECYCLE_TOPIC;

@ExtendWith(MockitoExtension.class)
class SensorStatusMonitorTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private SensorStateRepository sensorStateRepository;

    @Mock
    private KafkaTemplate<String, SensorLifecycleEvent> kafkaTemplate;

    @InjectMocks
    private SensorStatusMonitor sensorStatusMonitor;

    private org.zeus.dbo.SensorState disconnectedStateDbo;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sensorStatusMonitor, "heartbeatTimeoutSeconds", 5L);

        disconnectedStateDbo = new org.zeus.dbo.SensorState();
        disconnectedStateDbo.setStateId(2);
        disconnectedStateDbo.setStateName("DISCONNECTED");

        when(sensorStateRepository.findByStateName(SensorState.DISCONNECTED.getValue()))
                .thenReturn(Optional.of(disconnectedStateDbo));
    }

    @Test
    void checkSensorHeartbeats_whenStaleSensorsExist_updatesStatusAndPublishesEvents() {
        sensorStatusMonitor.init();

        Sensor staleSensor1 = new Sensor();
        staleSensor1.setSensorId("stale-sensor-1");
        Sensor staleSensor2 = new Sensor();
        staleSensor2.setSensorId("stale-sensor-2");
        List<Sensor> staleSensorsList = List.of(staleSensor1, staleSensor2);

        when(sensorRepository.findByState_StateNameAndLastUpdatedBefore(eq(SensorState.CONNECTED.getValue()), any(OffsetDateTime.class)))
                .thenReturn(staleSensorsList);

        sensorStatusMonitor.checkSensorHeartbeats();

        verify(sensorRepository).findByState_StateNameAndLastUpdatedBefore(eq(SensorState.CONNECTED.getValue()), any(OffsetDateTime.class));

        ArgumentCaptor<List<Sensor>> saveAllCaptor = ArgumentCaptor.forClass(List.class);
        verify(sensorRepository).saveAll(saveAllCaptor.capture());
        List<Sensor> savedSensors = saveAllCaptor.getValue();
        assertThat(savedSensors).hasSize(2);
        assertThat(savedSensors.get(0).getState()).isEqualTo(disconnectedStateDbo);
        assertThat(savedSensors.get(1).getState()).isEqualTo(disconnectedStateDbo);

        ArgumentCaptor<SensorLifecycleEvent> kafkaEventCaptor = ArgumentCaptor.forClass(SensorLifecycleEvent.class);
        verify(kafkaTemplate, times(2)).send(eq(SENSOR_LIFECYCLE_TOPIC), anyString(), kafkaEventCaptor.capture());

        List<SensorLifecycleEvent> capturedEvents = kafkaEventCaptor.getAllValues();
        assertThat(capturedEvents).hasSize(2);
        assertThat(capturedEvents.get(0).getSensorId()).isEqualTo("stale-sensor-1");
        assertThat(capturedEvents.get(0).getStatus()).isEqualTo("DISCONNECTED");
        assertThat(capturedEvents.get(1).getSensorId()).isEqualTo("stale-sensor-2");
        assertThat(capturedEvents.get(1).getStatus()).isEqualTo("DISCONNECTED");
    }

    @Test
    void checkSensorHeartbeats_whenNoStaleSensorsExist_doesNothing() {
        sensorStatusMonitor.init();

        when(sensorRepository.findByState_StateNameAndLastUpdatedBefore(eq(SensorState.CONNECTED.getValue()), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());

        sensorStatusMonitor.checkSensorHeartbeats();

        verify(sensorRepository).findByState_StateNameAndLastUpdatedBefore(eq(SensorState.CONNECTED.getValue()), any(OffsetDateTime.class));
        verify(sensorRepository, never()).saveAll(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
