package org.zeus.repository.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.AuditEventType;

import java.util.Optional;

@Repository
public interface AuditEventTypeRepository extends JpaRepository<AuditEventType, Integer> {

    /**
     * Finds an AuditEventType by its type name.
     *
     * @param typeName The name of the audit event type.
     * @return An Optional containing the AuditEventType if found, otherwise empty.
     */
    Optional<AuditEventType> findByTypeName(String typeName);
}