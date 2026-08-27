package com.pe.cloudapi.monitoring.domain.repositories;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de {@link RoomType}.
 */
public interface RoomTypeRepository {

    RoomType save(RoomType roomType);

    Optional<RoomType> findById(UUID id);

    Optional<RoomType> findBySiteIdAndCode(UUID siteId, String code);

    List<RoomType> findAllBySiteId(UUID siteId);
}
