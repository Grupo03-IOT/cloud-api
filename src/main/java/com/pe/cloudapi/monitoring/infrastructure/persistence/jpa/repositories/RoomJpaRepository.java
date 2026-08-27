package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de salas.
 */
@Repository
public interface RoomJpaRepository extends JpaRepository<RoomEntity, UUID> {

    /**
     * Busca por el código que trae el firmware en cada lote.
     */
    @Query("""
            SELECT r FROM RoomEntity r
            WHERE r.code = :code AND r.deletedAt IS NULL
            """)
    Optional<RoomEntity> findByCode(String code);

    @Query("""
            SELECT r FROM RoomEntity r
            WHERE r.siteId = :siteId AND r.code = :code AND r.deletedAt IS NULL
            """)
    Optional<RoomEntity> findBySiteIdAndCode(UUID siteId, String code);

    @Query("""
            SELECT r FROM RoomEntity r
            WHERE r.siteId = :siteId AND r.active = TRUE AND r.deletedAt IS NULL
            ORDER BY r.displayName ASC
            """)
    List<RoomEntity> findActiveBySiteId(UUID siteId);

    /**
     * Salas dadas de alta automáticamente que nadie ha clasificado todavía.
     * Es la bandeja de trabajo del administrador en la aplicación web.
     */
    @Query("""
            SELECT r FROM RoomEntity r
            WHERE r.roomTypeId IS NULL AND r.deletedAt IS NULL
            """)
    List<RoomEntity> findUnclassified();
}
