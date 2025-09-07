package org.zeus.repository.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.SensorState;

import java.util.Optional;

@Repository
public interface SensorStateRepository extends JpaRepository<SensorState, Integer> {

    /**
     * Finds a SensorState entity by its state name.
     * Spring Data JPA automatically implements this method based on its name.
     *
     * @param stateName The name of the state to find (e.g., "DISCONNECTED").
     * @return An Optional containing the found SensorState, or empty if not found.
     */
    Optional<SensorState> findByStateName(String stateName);
}