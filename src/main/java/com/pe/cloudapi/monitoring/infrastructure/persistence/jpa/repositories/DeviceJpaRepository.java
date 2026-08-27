package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de dispositivos.
 */
@Repository
public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, UUID> {

    @Query("""
            SELECT d FROM DeviceEntity d
            WHERE d.code = :code AND d.deletedAt IS NULL
            """)
    Optional<DeviceEntity> findByCode(String code);

    @Query("""
            SELECT d FROM DeviceEntity d
            WHERE d.roomId = :roomId AND d.deletedAt IS NULL
            """)
    List<DeviceEntity> findAllByRoomId(UUID roomId);

    /**
     * Dispositivos que llevan sin reportar desde el instante dado. Sirve para
     * marcarlos como caídos sin depender de que ellos avisen.
     */
    @Query("""
            SELECT d FROM DeviceEntity d
            WHERE d.deletedAt IS NULL
              AND (d.lastSeen IS NULL OR d.lastSeen < :since)
            """)
    List<DeviceEntity> findSilentSince(OffsetDateTime since);
}
