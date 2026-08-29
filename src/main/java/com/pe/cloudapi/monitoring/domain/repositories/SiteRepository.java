package com.pe.cloudapi.monitoring.domain.repositories;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de {@link Site}. Se implementa en la capa de
 * infraestructura; el dominio no conoce JPA.
 */
public interface SiteRepository {

    Site save(Site site);

    Optional<Site> findById(UUID id);

    Optional<Site> findByCode(String code);

    List<Site> findAll();

    /**
     * Local por defecto. Mientras el producto atienda un solo coworking, es el
     * único que existe.
     */
    Optional<Site> findDefault();
}
