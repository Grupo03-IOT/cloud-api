package com.pe.cloudapi.monitoring.domain.model.queries;

import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;

import java.util.UUID;

/**
 * La lectura más reciente de una sala.
 *
 * @param roomId sala consultada
 */
public record GetLatestReadingQuery(UUID roomId) {

    public GetLatestReadingQuery {
        if (roomId == null) {
            throw MonitoringError.ROOM_REQUIRED.with();
        }
    }
}
