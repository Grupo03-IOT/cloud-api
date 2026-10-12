package com.pe.cloudapi.insights.domain.model.queries;

import com.pe.cloudapi.insights.domain.model.valueobjects.Period;

import java.util.Objects;
import java.util.UUID;

/**
 * Análisis de una sala en un periodo.
 */
public record AnalyzeRoomQuery(UUID roomId, Period period) {

    public AnalyzeRoomQuery {
        Objects.requireNonNull(roomId, "roomId");
        Objects.requireNonNull(period, "period");
    }
}
