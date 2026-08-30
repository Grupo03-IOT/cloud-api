package com.pe.cloudapi.monitoring.domain.model.queries;

import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Serie temporal de una sala entre dos instantes.
 *
 * <p>El rango invertido se rechaza aquí porque es un invariante: un rango cuyo
 * inicio va después del fin no significa nada, venga de donde venga.
 *
 * <p>Los nulos, en cambio, no son error de negocio sino de programación: la
 * capa REST ya garantiza que lleguen, así que un nulo aquí es un bug y sale
 * como tal.
 *
 * @param roomId sala consultada
 * @param from   inicio del rango, inclusive
 * @param to     fin del rango, inclusive
 */
public record GetReadingsInRangeQuery(UUID roomId, OffsetDateTime from, OffsetDateTime to) {

    public GetReadingsInRangeQuery {
        Objects.requireNonNull(roomId, "roomId");
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (from.isAfter(to)) {
            throw MonitoringError.RANGE_INVERTED.with(from, to);
        }
    }
}
