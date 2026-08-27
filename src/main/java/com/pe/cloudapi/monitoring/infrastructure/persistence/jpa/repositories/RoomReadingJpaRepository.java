package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.RoomReadingEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de lecturas por minuto.
 *
 * <p>Sin columnas de auditoría ni borrado lógico: la telemetría es inmutable.
 */
@Repository
public interface RoomReadingJpaRepository extends JpaRepository<RoomReadingEntity, UUID> {

    /**
     * Clave natural de la lectura. La ingesta la usa para deduplicar: el Edge
     * reintenta cuando no confirma la subida, así que el mismo minuto puede
     * llegar más de una vez.
     */
    Optional<RoomReadingEntity> findByRoomIdAndTs(UUID roomId, OffsetDateTime ts);

    @Query("""
            SELECT rr FROM RoomReadingEntity rr
            WHERE rr.roomId = :roomId AND rr.ts BETWEEN :from AND :to
            ORDER BY rr.ts ASC
            """)
    List<RoomReadingEntity> findInRange(UUID roomId, OffsetDateTime from, OffsetDateTime to);

    @Query("""
            SELECT rr FROM RoomReadingEntity rr
            WHERE rr.roomId = :roomId
            ORDER BY rr.ts DESC
            LIMIT 1
            """)
    Optional<RoomReadingEntity> findLatest(UUID roomId);
}
