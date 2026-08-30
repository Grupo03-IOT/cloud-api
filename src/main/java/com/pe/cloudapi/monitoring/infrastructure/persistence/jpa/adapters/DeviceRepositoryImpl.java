package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.monitoring.domain.model.entities.Device;
import com.pe.cloudapi.monitoring.domain.ports.out.DeviceRepository;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.DeviceEntity;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers.DeviceMapper;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories.DeviceJpaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador del puerto {@link DeviceRepository} sobre Spring Data.
 */
@Component
@RequiredArgsConstructor
public class DeviceRepositoryImpl implements DeviceRepository {

    private final DeviceJpaRepository jpa;
    private final DeviceMapper mapper;

    @Override
    public Device save(Device device) {
        DeviceEntity entity = device.getId() == null
                ? new DeviceEntity()
                : jpa.findById(device.getId()).orElseGet(DeviceEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, device)));
    }

    @Override
    public Optional<Device> findByCode(String code) {
        return jpa.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<Device> findAllByRoomId(UUID roomId) {
        return jpa.findAllByRoomId(roomId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Device> findSilentSince(OffsetDateTime since) {
        return jpa.findSilentSince(since).stream().map(mapper::toDomain).toList();
    }
}
