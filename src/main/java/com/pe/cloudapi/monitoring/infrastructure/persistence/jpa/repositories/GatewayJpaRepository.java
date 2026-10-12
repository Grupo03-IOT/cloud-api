package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.GatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de Edges.
 */
@Repository
public interface GatewayJpaRepository extends JpaRepository<GatewayEntity, UUID> {

    @Query("""
            SELECT g FROM GatewayEntity g
            WHERE g.code = :code AND g.deletedAt IS NULL
            """)
    Optional<GatewayEntity> findByCode(String code);

    @Query("""
            SELECT g FROM GatewayEntity g
            WHERE g.deletedAt IS NULL
            ORDER BY g.code
            """)
    List<GatewayEntity> findAllAlive();
}
