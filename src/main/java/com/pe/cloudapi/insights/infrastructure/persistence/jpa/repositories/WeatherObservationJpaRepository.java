package com.pe.cloudapi.insights.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.insights.infrastructure.persistence.jpa.entities.WeatherObservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de observaciones exteriores.
 */
@Repository
public interface WeatherObservationJpaRepository
        extends JpaRepository<WeatherObservationEntity, UUID> {

    @Query("""
            SELECT w FROM WeatherObservationEntity w
            WHERE w.observedAt BETWEEN :from AND :to
            ORDER BY w.observedAt ASC
            """)
    List<WeatherObservationEntity> findInRange(OffsetDateTime from, OffsetDateTime to);

    /** Evita guardar dos veces la misma medición: el proveedor la repite entre consultas. */
    Optional<WeatherObservationEntity> findByObservedAt(OffsetDateTime observedAt);
}
