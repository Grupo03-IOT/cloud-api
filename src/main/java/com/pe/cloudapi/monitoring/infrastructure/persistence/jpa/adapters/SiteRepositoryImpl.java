package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.SiteEntity;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers.SiteMapper;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories.SiteJpaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador del puerto {@link SiteRepository} sobre Spring Data.
 *
 * <p>Al guardar, si el modelo de dominio ya trae identificador se recarga la
 * entidad en vez de crear una nueva: el dominio no conoce las columnas de
 * auditoría, y sin recargar se perderían {@code createdAt} y {@code createdBy}.
 */
@Component
@RequiredArgsConstructor
public class SiteRepositoryImpl implements SiteRepository {

    private final SiteJpaRepository jpa;
    private final SiteMapper mapper;

    @Override
    public Site save(Site site) {
        SiteEntity entity = site.getId() == null
                ? new SiteEntity()
                : jpa.findById(site.getId()).orElseGet(SiteEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, site)));
    }

    @Override
    public Optional<Site> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Site> findByCode(String code) {
        return jpa.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<Site> findAll() {
        return jpa.findAllAlive().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Site> findDefault() {
        return jpa.findDefault().map(mapper::toDomain);
    }
}
