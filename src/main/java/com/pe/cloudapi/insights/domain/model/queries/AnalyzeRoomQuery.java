package com.pe.cloudapi.insights.domain.model.queries;

import com.pe.cloudapi.insights.domain.model.errors.InsightsError;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Análisis de una sala en un periodo.
 *
 * @param roomId sala analizada
 * @param from   inicio del rango, inclusive
 * @param to     fin del rango, inclusive
 */
public record AnalyzeRoomQuery(UUID roomId, OffsetDateTime from, OffsetDateTime to) {

    public AnalyzeRoomQuery {
        Objects.requireNonNull(roomId, "roomId");
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (from.isAfter(to)) {
            throw InsightsError.RANGE_INVERTED.with(from, to);
        }
    }
}
