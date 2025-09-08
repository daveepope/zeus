package org.zeus.repository.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.MetricType;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link MetricType} entity.
 * Provides methods for querying and managing metric type data.
 */
@Repository
public interface MetricTypeRepository extends JpaRepository<MetricType, Integer> {

    /**
     * Finds a MetricType by its type name.
     *
     * @param typeName The type name of the metric.
     * @return An Optional containing the MetricType if found, otherwise an empty Optional.
     */
    Optional<MetricType> findByTypeName(String typeName);
}
