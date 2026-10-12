package com.pe.cloudapi.monitoring.domain.model.queries;

import java.util.Objects;
import java.util.UUID;

/**
 * Las salas activas de un local.
 *
 * @param siteId local consultado
 */
public record ListSiteRoomsQuery(UUID siteId) {

    public ListSiteRoomsQuery {
        Objects.requireNonNull(siteId, "siteId");
    }
}
