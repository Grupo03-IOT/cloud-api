package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.SiteEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de locales.
 */
@Repository
public interface SiteJpaRepository extends JpaRepository<SiteEntity, UUID> {

    @Query("""
            SELECT s FROM SiteEntity s
            WHERE s.code = :code AND s.deletedAt IS NULL
            """)
    Optional<SiteEntity> findByCode(String code);

    /**
     * Los locales vivos, del más antiguo al más reciente.
     *
     * <p>No es el {@code findAll} heredado de Spring Data: ese devolvería
     * también los borrados.
     */
    @Query("""
            SELECT s FROM SiteEntity s
            WHERE s.deletedAt IS NULL
            ORDER BY s.createdAt ASC
            """)
    List<SiteEntity> findAllAlive();

    /**
     * Local por defecto: el más antiguo que siga vivo. Mientras el producto
     * atienda un solo coworking, es el único que hay.
     */
    @Query("""
            SELECT s FROM SiteEntity s
            WHERE s.deletedAt IS NULL
            ORDER BY s.createdAt ASC
            LIMIT 1
            """)
    Optional<SiteEntity> findDefault();
}
