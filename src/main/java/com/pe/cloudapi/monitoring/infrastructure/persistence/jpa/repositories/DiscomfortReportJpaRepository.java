package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.DiscomfortReportEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de reportes de incomodidad.
 */
@Repository
public interface DiscomfortReportJpaRepository extends JpaRepository<DiscomfortReportEntity, UUID>,
        JpaSpecificationExecutor<DiscomfortReportEntity> {

    Optional<DiscomfortReportEntity> findFirstByUserIdAndRoomIdAndCategoryOrderByCreatedAtDesc(
            UUID userId, UUID roomId, String category);

    List<DiscomfortReportEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
