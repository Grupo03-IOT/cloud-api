package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Gateway;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.GatewayEntity;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el agregado {@code Gateway} y su entidad JPA.
 */
@Component
public class GatewayMapper {

    public Gateway toDomain(GatewayEntity entity) {
        if (entity == null) return null;
        return Gateway.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .siteId(entity.getSiteId())
                .version(entity.getVersion())
                .lastSeen(entity.getLastSeen())
                .pendingUpload(entity.getPendingUpload() == null ? 0 : entity.getPendingUpload())
                .roomsReported(entity.getRoomsReported() == null ? 0 : entity.getRoomsReported())
                .build();
    }

    public GatewayEntity applyTo(GatewayEntity entity, Gateway domain) {
        entity.setCode(domain.getCode());
        entity.setSiteId(domain.getSiteId());
        entity.setVersion(domain.getVersion());
        entity.setLastSeen(domain.getLastSeen());
        entity.setPendingUpload(domain.getPendingUpload());
        entity.setRoomsReported(domain.getRoomsReported());
        return entity;
    }
}
