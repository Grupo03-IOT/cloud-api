package com.pe.cloudapi.alerting.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.alerting.infrastructure.persistence.jpa.entities.AlertEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de alertas. Los filtros opcionales del listado se
 * arman con especificaciones para no concatenar consultas.
 */
@Repository
public interface AlertJpaRepository extends JpaRepository<AlertEntity, UUID>,
        JpaSpecificationExecutor<AlertEntity> {

    Optional<AlertEntity> findBySourceKey(String sourceKey);
}
