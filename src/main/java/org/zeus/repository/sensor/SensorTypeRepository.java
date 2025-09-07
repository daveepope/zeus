package org.zeus.repository.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.SensorType;

import java.util.Optional;

@Repository
public interface SensorTypeRepository extends JpaRepository<SensorType, Integer> {
    // JpaRepository provides all standard CRUD operations like findById, save, etc.
    // No additional methods are needed for the current functionality.

    Optional<SensorType> findByTypeName(String typeName);
}