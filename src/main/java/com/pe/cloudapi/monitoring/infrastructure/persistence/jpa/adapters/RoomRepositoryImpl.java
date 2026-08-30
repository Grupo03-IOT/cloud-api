package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.RoomEntity;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers.RoomMapper;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories.RoomJpaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador del puerto {@link RoomRepository} sobre Spring Data.
 */
@Component
@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepository {

    private final RoomJpaRepository jpa;
    private final RoomMapper mapper;

    @Override
    public Room save(Room room) {
        RoomEntity entity = room.getId() == null
                ? new RoomEntity()
                : jpa.findById(room.getId()).orElseGet(RoomEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, room)));
    }

    @Override
    public Optional<Room> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Room> findByCode(String code) {
        return jpa.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public Optional<Room> findBySiteIdAndCode(UUID siteId, String code) {
        return jpa.findBySiteIdAndCode(siteId, code).map(mapper::toDomain);
    }

    @Override
    public List<Room> findActiveBySiteId(UUID siteId) {
        return jpa.findActiveBySiteId(siteId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Room> findUnclassified() {
        return jpa.findUnclassified().stream().map(mapper::toDomain).toList();
    }
}
