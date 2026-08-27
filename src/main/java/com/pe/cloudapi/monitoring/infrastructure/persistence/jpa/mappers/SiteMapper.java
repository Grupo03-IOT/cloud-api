package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.SiteEntity;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el agregado {@code Site} y su entidad JPA.
 */
@Component
public class SiteMapper {

    public Site toDomain(SiteEntity entity) {
        if (entity == null) return null;
        return Site.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .address(entity.getAddress())
                .timezone(entity.getTimezone())
                .build();
    }

    public SiteEntity toEntity(Site domain) {
        return applyTo(new SiteEntity(), domain);
    }

    public SiteEntity applyTo(SiteEntity entity, Site domain) {
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setAddress(domain.getAddress());
        entity.setTimezone(domain.getTimezone());
        return entity;
    }
}
