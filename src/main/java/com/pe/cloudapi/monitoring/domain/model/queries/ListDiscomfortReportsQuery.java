package com.pe.cloudapi.monitoring.domain.model.queries;

import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Filtros del listado de reportes de incomodidad. Todos opcionales.
 *
 * @param roomId   sala
 * @param category motivo
 * @param from     creado desde, inclusive
 * @param to       creado hasta, inclusive
 * @param discrepancy {@code true}: solo con discrepancia; {@code false}: solo sin ella
 */
public record ListDiscomfortReportsQuery(UUID roomId, DiscomfortCategory category,
                                         OffsetDateTime from, OffsetDateTime to,
                                         Boolean discrepancy) {

    public ListDiscomfortReportsQuery {
        if (from != null && to != null && from.isAfter(to)) {
            throw MonitoringError.RANGE_INVERTED.with(from, to);
        }
    }

    public static ListDiscomfortReportsQuery between(OffsetDateTime from, OffsetDateTime to) {
        return new ListDiscomfortReportsQuery(null, null, from, to, null);
    }
}
