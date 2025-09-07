package org.zeus.repository.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.SensorAuditEvent;

@Repository
public interface SensorAuditEventRepository extends JpaRepository<SensorAuditEvent, Long> {
    // JpaRepository provides all standard CRUD operations for SensorAuditEvent.
    // No additional methods are needed for the current functionality.
}