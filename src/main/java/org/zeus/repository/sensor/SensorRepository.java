package org.zeus.repository.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.Sensor;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, String> {
    /**
     * Counts the number of sensors whose sensor ID is in the provided list.
     * This is used to validate the existence of multiple sensors.
     *
     * @param sensorIds A list of sensor IDs to count.
     * @return The number of sensors found with a matching ID.
     */
    long countBySensorIdIn(List<String> sensorIds);

    /**
     * Finds sensors by their state's name and a lastUpdated timestamp before a certain time.
     * The name follows Spring Data JPA conventions for querying nested properties (Sensor -> State -> stateName).
     * This is used by the SensorStatusMonitor to find sensors that have missed their heartbeat.
     *
     * @param stateName The name of the state to check (e.g., "CONNECTED").
     * @param cutoffTime The time threshold. Any sensor last updated before this time is considered stale.
     * @return A list of stale sensors.
     */
    List<Sensor> findByState_StateNameAndLastUpdatedBefore(String stateName, OffsetDateTime cutoffTime);
}