package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.repositories.RoomTypeRepository;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.RoomTypeEntity;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers.RoomTypeMapper;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories.RoomTypeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador del puerto {@link RoomTypeRepository} sobre Spring Data.
 */
@Component
@RequiredArgsConstructor
public class RoomTypeRepositoryAdapter implements RoomTypeRepository {

    private final RoomTypeJpaRepository jpa;
    private final RoomTypeMapper mapper;

    @Override
    public RoomType save(RoomType roomType) {
        RoomTypeEntity entity = roomType.getId() == null
                ? new RoomTypeEntity()
                : jpa.findById(roomType.getId()).orElseGet(RoomTypeEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, roomType)));
    }

    @Override
    public Optional<RoomType> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<RoomType> findBySiteIdAndCode(UUID siteId, String code) {
        return jpa.findBySiteIdAndCode(siteId, code).map(mapper::toDomain);
    }

    @Override
    public List<RoomType> findAllBySiteId(UUID siteId) {
        return jpa.findAllBySiteId(siteId).stream().map(mapper::toDomain).toList();
    }
}
