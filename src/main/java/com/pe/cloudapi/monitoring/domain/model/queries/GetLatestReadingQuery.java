package com.pe.cloudapi.monitoring.domain.model.queries;

import java.util.Objects;
import java.util.UUID;

/**
 * La lectura más reciente de una sala.
 *
 * @param roomId sala consultada
 */
public record GetLatestReadingQuery(UUID roomId) {

    public GetLatestReadingQuery {
        Objects.requireNonNull(roomId, "roomId");
    }
}
