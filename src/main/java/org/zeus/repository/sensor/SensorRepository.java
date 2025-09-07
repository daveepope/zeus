package org.zeus.repository.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.Sensor;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, String> {
    // JpaRepository provides all standard CRUD operations like findById, save, etc.
    // No additional methods are needed for the current functionality.
}