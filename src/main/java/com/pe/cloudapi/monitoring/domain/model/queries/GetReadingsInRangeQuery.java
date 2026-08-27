package com.pe.cloudapi.monitoring.domain.model.queries;

import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Serie temporal de una sala entre dos instantes.
 *
 * <p>Se valida en el propio objeto: un rango invertido no es un caso de negocio
 * sino un error de quien llama, y conviene que falle antes de tocar la base.
 *
 * @param roomId sala consultada
 * @param from   inicio del rango, inclusive
 * @param to     fin del rango, inclusive
 */
public record GetReadingsInRangeQuery(UUID roomId, OffsetDateTime from, OffsetDateTime to) {

    public GetReadingsInRangeQuery {
        if (roomId == null) {
            throw MonitoringError.ROOM_REQUIRED.with();
        }
        if (from == null || to == null) {
            throw MonitoringError.RANGE_REQUIRED.with();
        }
        if (from.isAfter(to)) {
            throw MonitoringError.RANGE_INVERTED.with(from, to);
        }
    }
}
