 package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.monitoring.domain.model.entities.Device;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.DeviceEntity;
import org.springframework.stereotype.Component;

/**
 * Traduce entre la entidad de dominio {@code Device} y su entidad JPA.
 *
 * <p>No mapea la columna {@code status} de la tabla: solo se guardan
 * observaciones, y si un dispositivo está caído es una lectura de ellas.
 */
@Component
public class DeviceMapper {

    public Device toDomain(DeviceEntity entity) {
        if (entity == null) return null;
        return Device.builder()
                .id(entity.getId())
                .roomId(entity.getRoomId())
                .code(entity.getCode())
                .fwVersion(entity.getFwVersion())
                .lastSeen(entity.getLastSeen())
                .lastSeq(entity.getLastSeq() == null ? -1L : entity.getLastSeq())
                .lostBatches(entity.getLostBatches() == null ? 0L : entity.getLostBatches())
                .build();
    }

    public DeviceEntity toEntity(Device domain) {
        return applyTo(new DeviceEntity(), domain);
    }

    public DeviceEntity applyTo(DeviceEntity entity, Device domain) {
        entity.setRoomId(domain.getRoomId());
        entity.setCode(domain.getCode());
        entity.setFwVersion(domain.getFwVersion());
        entity.setLastSeen(domain.getLastSeen());
        entity.setLastSeq(domain.getLastSeq());
        entity.setLostBatches(domain.getLostBatches());
        return entity;
    }
}
