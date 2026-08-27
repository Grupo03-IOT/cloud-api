package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.RoomTypeEntity;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el agregado {@code RoomType} y su entidad JPA.
 */
@Component
public class RoomTypeMapper {

    public RoomType toDomain(RoomTypeEntity entity) {
        if (entity == null) return null;
        return RoomType.builder()
                .id(entity.getId())
                .siteId(entity.getSiteId())
                .code(entity.getCode())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .build();
    }

    public RoomTypeEntity toEntity(RoomType domain) {
        return applyTo(new RoomTypeEntity(), domain);
    }

    public RoomTypeEntity applyTo(RoomTypeEntity entity, RoomType domain) {
        entity.setSiteId(domain.getSiteId());
        entity.setCode(domain.getCode());
        entity.setDisplayName(domain.getDisplayName());
        entity.setDescription(domain.getDescription());
        return entity;
    }
}
