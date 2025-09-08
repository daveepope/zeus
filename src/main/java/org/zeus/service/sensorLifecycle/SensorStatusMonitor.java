package org.zeus.service.sensorLifecycle;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zeus.dbo.Sensor;
import org.zeus.domain.event.SensorLifecycleEvent;
import org.zeus.model.SensorState;
import org.zeus.repository.sensor.SensorRepository;
import org.zeus.repository.sensor.SensorStateRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static org.zeus.domain.event.DomainConstants.SENSOR_LIFECYCLE_TOPIC;

@Component
@RequiredArgsConstructor
public class SensorStatusMonitor {

    private static final Logger log = LoggerFactory.getLogger(SensorStatusMonitor.class);

    private final SensorRepository sensorRepository;
    private final SensorStateRepository sensorStateRepository;
    private final KafkaTemplate<String, SensorLifecycleEvent> kafkaTemplate;

    @Value("${sensor.heartbeat.timeout.seconds:5}")
    private long heartbeatTimeoutSeconds;

    private org.zeus.dbo.SensorState disconnectedState;

    /**
     * On startup, fetch the DBO for the 'DISCONNECTED' state from the database
     * so we don't have to query for it on every scheduled run.
     */
    @PostConstruct
    public void init() {
        this.disconnectedState = sensorStateRepository.findByStateName(SensorState.DISCONNECTED.getValue())
                .orElseThrow(() -> new IllegalStateException("State 'DISCONNECTED' not found in database."));
    }

    @Scheduled(fixedRateString = "${sensor.monitor.rate.ms:5000}")
    public void checkSensorHeartbeats() {
        log.debug("Running sensor heartbeat check for stale sensors...");

        OffsetDateTime cutoffTime = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(heartbeatTimeoutSeconds);

        List<Sensor> staleSensors = sensorRepository.findByState_StateNameAndLastUpdatedBefore(
                SensorState.CONNECTED.getValue(),
                cutoffTime
        );

        if (staleSensors.isEmpty()) {
            return;
        }

        log.warn("Found {} stale sensors. Updating status to DISCONNECTED.", staleSensors.size());

        for (Sensor sensor : staleSensors) {
            sensor.setState(this.disconnectedState);
            publishSensorLifecycleEvent(sensor.getSensorId(), SensorState.DISCONNECTED.getValue());
        }

        sensorRepository.saveAll(staleSensors);
    }

    private void publishSensorLifecycleEvent(String sensorId, String status) {
        SensorLifecycleEvent event = SensorLifecycleEvent.builder()
                .sensorId(sensorId)
                .status(status)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        kafkaTemplate.send(SENSOR_LIFECYCLE_TOPIC, sensorId, event);
        log.info("Published SensorLifecycleEvent for sensorId {} with status {}", sensorId, status);
    }
}
