package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.RoomTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de tipos de sala.
 */
@Repository
public interface RoomTypeJpaRepository extends JpaRepository<RoomTypeEntity, UUID> {

    @Query("""
            SELECT rt FROM RoomTypeEntity rt
            WHERE rt.siteId = :siteId AND rt.code = :code AND rt.deletedAt IS NULL
            """)
    Optional<RoomTypeEntity> findBySiteIdAndCode(UUID siteId, String code);

    @Query("""
            SELECT rt FROM RoomTypeEntity rt
            WHERE rt.siteId = :siteId AND rt.deletedAt IS NULL
            ORDER BY rt.displayName ASC
            """)
    List<RoomTypeEntity> findAllBySiteId(UUID siteId);
}
