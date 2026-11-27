package com.pe.cloudapi.alerting.domain.model.queries;

import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Filtros del listado de alertas. Todos opcionales; {@code null} no filtra.
 *
 * @param status    estado de la alerta
 * @param roomId    sala
 * @param from      apertura desde, inclusive
 * @param to        apertura hasta, inclusive
 * @param hasAction {@code true} solo las que tienen acción correctiva,
 *                  {@code false} solo las que no
 */
public record ListAlertsQuery(AlertStatus status, UUID roomId, OffsetDateTime from,
                              OffsetDateTime to, Boolean hasAction) {

    public ListAlertsQuery {
        if (from != null && to != null && from.isAfter(to)) {
            throw AlertingError.RANGE_INVERTED.with(from, to);
        }
    }
}
