package org.zeus.service.sensorLifecycle;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.zeus.model.SensorState;

@Service
@RequiredArgsConstructor
public class AsyncSensorLifecycleOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AsyncSensorLifecycleOrchestrator.class);

    private final SensorLifecycleOrchestrator sensorLifecycleOrchestrator;

    /**
     * Processes a sensor heartbeat on a background thread.
     * @param sensorId The ID of the sensor.
     * @param newState The new state reported by the sensor.
     */
    @Async
    public void processHeartbeatAsync(String sensorId, SensorState newState) {
        try {
            log.info("Handing off heartbeat processing to background thread for sensor ID: {}", sensorId);

            sensorLifecycleOrchestrator.processHeartbeat(sensorId, newState);

            log.info("Successfully completed async heartbeat for sensor ID: {}", sensorId);
        } catch (Exception e) {
            // This try-catch is crucial for logging errors that happen on the background thread.
            log.error("Async heartbeat processing failed for sensor ID: " + sensorId, e);
        }
    }
}
