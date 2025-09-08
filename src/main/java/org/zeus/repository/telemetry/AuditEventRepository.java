package org.zeus.repository.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.SensorAuditEvent;

import java.util.Optional;

@Repository
public interface AuditEventRepository extends JpaRepository<SensorAuditEvent, Integer> {
}