package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.ThresholdEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de umbrales.
 */
@Repository
public interface ThresholdJpaRepository extends JpaRepository<ThresholdEntity, UUID> {

    @Query("""
            SELECT t FROM ThresholdEntity t
            WHERE t.roomTypeId = :roomTypeId AND t.enabled = TRUE AND t.deletedAt IS NULL
            """)
    List<ThresholdEntity> findEnabledByRoomTypeId(UUID roomTypeId);

    @Query("""
            SELECT t FROM ThresholdEntity t
            WHERE t.roomTypeId = :roomTypeId AND t.metric = :metric AND t.deletedAt IS NULL
            """)
    Optional<ThresholdEntity> findByRoomTypeIdAndMetric(UUID roomTypeId, String metric);

    /**
     * Umbrales aplicables a una sala, resueltos a través de su tipo. Devuelve
     * vacío si la sala todavía no está clasificada.
     */
    @Query("""
            SELECT t FROM ThresholdEntity t, RoomEntity r
            WHERE r.id = :roomId
              AND t.roomTypeId = r.roomTypeId
              AND t.enabled = TRUE
              AND t.deletedAt IS NULL
            """)
    List<ThresholdEntity> findApplicableToRoom(UUID roomId);
}
