package com.pe.cloudapi.monitoring.domain.model.queries;

import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;

import java.util.UUID;

/**
 * Una sala concreta, con su última lectura.
 *
 * @param roomId sala consultada
 */
public record GetRoomQuery(UUID roomId) {

    public GetRoomQuery {
        if (roomId == null) {
            throw MonitoringError.ROOM_REQUIRED.with();
        }
    }
}
