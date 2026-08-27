package com.pe.cloudapi.monitoring.domain.repositories;

import com.pe.cloudapi.monitoring.domain.model.entities.Device;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de {@link Device}.
 */
public interface DeviceRepository {

    Device save(Device device);

    Optional<Device> findByCode(String code);

    List<Device> findAllByRoomId(UUID roomId);

    /**
     * Dispositivos que llevan sin reportar desde el instante dado. Sirve para
     * marcarlos caídos sin depender de que ellos avisen.
     */
    List<Device> findSilentSince(OffsetDateTime since);
}
