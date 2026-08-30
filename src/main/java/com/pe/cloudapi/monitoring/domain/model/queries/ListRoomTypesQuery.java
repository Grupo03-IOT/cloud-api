package com.pe.cloudapi.monitoring.domain.model.queries;

import java.util.Objects;
import java.util.UUID;

/**
 * Tipos de sala de un local.
 *
 * @param siteId local consultado
 */
public record ListRoomTypesQuery(UUID siteId) {

    public ListRoomTypesQuery {
        Objects.requireNonNull(siteId, "siteId");
    }
}
