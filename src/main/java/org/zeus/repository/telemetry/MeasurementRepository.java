package org.zeus.repository.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.SensorMeasurement;

/**
 * Spring Data JPA repository for the Measurement entity.
 * It provides standard CRUD operations and custom queries for sensor measurements.
 */
@Repository
public interface MeasurementRepository extends JpaRepository<SensorMeasurement, Integer> {

}
