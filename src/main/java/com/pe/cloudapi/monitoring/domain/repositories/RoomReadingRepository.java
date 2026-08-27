package com.pe.cloudapi.monitoring.domain.repositories;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de {@link RoomReading}.
 */
public interface RoomReadingRepository {

    RoomReading save(RoomReading reading);

    /**
     * Clave natural de la lectura. La ingesta la usa para deduplicar: el Edge
     * reintenta cuando no confirma la subida, así que el mismo minuto puede
     * llegar más de una vez.
     */
    Optional<RoomReading> findByRoomIdAndTs(UUID roomId, OffsetDateTime ts);

    List<RoomReading> findInRange(UUID roomId, OffsetDateTime from, OffsetDateTime to);

    Optional<RoomReading> findLatest(UUID roomId);
}
