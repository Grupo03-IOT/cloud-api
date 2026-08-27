package com.pe.cloudapi.monitoring.domain.repositories;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de {@link Room}.
 */
public interface RoomRepository {

    Room save(Room room);

    Optional<Room> findById(UUID id);

    /**
     * Busca por el código que trae el firmware en cada lote.
     */
    Optional<Room> findByCode(String code);

    Optional<Room> findBySiteIdAndCode(UUID siteId, String code);

    List<Room> findActiveBySiteId(UUID siteId);

    /**
     * Salas dadas de alta automáticamente que nadie ha clasificado todavía.
     */
    List<Room> findUnclassified();
}
