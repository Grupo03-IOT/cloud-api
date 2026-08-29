package com.pe.cloudapi.monitoring.domain.model.queries;

import java.util.Objects;
import java.util.UUID;

/**
 * Una sala concreta, con su última lectura.
 *
 * @param roomId sala consultada
 */
public record GetRoomQuery(UUID roomId) {

    public GetRoomQuery {
        Objects.requireNonNull(roomId, "roomId");
    }
}
