package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.RoomEntity;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el agregado {@code Room} y su entidad JPA.
 */
@Component
public class RoomMapper {

    public Room toDomain(RoomEntity entity) {
        if (entity == null) return null;
        return Room.builder()
                .id(entity.getId())
                .siteId(entity.getSiteId())
                .roomTypeId(entity.getRoomTypeId())
                .code(entity.getCode())
                .displayName(entity.getDisplayName())
                .floor(entity.getFloor())
                .capacity(entity.getCapacity())
                .areaM2(entity.getAreaM2())
                .active(Boolean.TRUE.equals(entity.getActive()))
                .build();
    }

    public RoomEntity toEntity(Room domain) {
        return applyTo(new RoomEntity(), domain);
    }

    public RoomEntity applyTo(RoomEntity entity, Room domain) {
        entity.setSiteId(domain.getSiteId());
        entity.setRoomTypeId(domain.getRoomTypeId());
        entity.setCode(domain.getCode());
        entity.setDisplayName(domain.getDisplayName());
        entity.setFloor(domain.getFloor());
        entity.setCapacity(domain.getCapacity());
        entity.setAreaM2(domain.getAreaM2());
        entity.setActive(domain.isActive());
        return entity;
    }
}
