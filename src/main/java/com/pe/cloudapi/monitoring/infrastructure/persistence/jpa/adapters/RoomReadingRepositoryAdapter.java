package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.domain.repositories.RoomReadingRepository;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.RoomReadingEntity;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers.RoomReadingMapper;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories.RoomReadingJpaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador del puerto {@link RoomReadingRepository} sobre Spring Data.
 */
@Component
@RequiredArgsConstructor
public class RoomReadingRepositoryAdapter implements RoomReadingRepository {

    private final RoomReadingJpaRepository jpa;
    private final RoomReadingMapper mapper;

    @Override
    public RoomReading save(RoomReading reading) {
        RoomReadingEntity entity = reading.getId() == null
                ? new RoomReadingEntity()
                : jpa.findById(reading.getId()).orElseGet(RoomReadingEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, reading)));
    }

    @Override
    public Optional<RoomReading> findByRoomIdAndTs(UUID roomId, OffsetDateTime ts) {
        return jpa.findByRoomIdAndTs(roomId, ts).map(mapper::toDomain);
    }

    @Override
    public List<RoomReading> findInRange(UUID roomId, OffsetDateTime from, OffsetDateTime to) {
        return jpa.findInRange(roomId, from, to).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<RoomReading> findLatest(UUID roomId) {
        return jpa.findLatest(roomId).map(mapper::toDomain);
    }
}
