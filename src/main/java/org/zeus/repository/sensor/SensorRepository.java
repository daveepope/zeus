package org.zeus.repository.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.Sensor;

import java.util.List;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, String> {
    // JpaRepository provides all standard CRUD operations like findById, save, etc.
    // No additional methods are needed for the current functionality.
    /**
     * Counts the number of sensors whose sensor ID is in the provided list.
     * This is used to validate the existence of multiple sensors.
     *
     * @param sensorIds A list of sensor IDs to count.
     * @return The number of sensors found with a matching ID.
     */
    long countBySensorIdIn(List<String> sensorIds);
}