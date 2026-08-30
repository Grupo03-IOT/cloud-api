package com.pe.cloudapi.insights.application.internal.ports.in;

import com.pe.cloudapi.insights.domain.model.queries.AnalyzeRoomQuery;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomAnalytics;

/**
 * Puerto de entrada: analizar una sala en un periodo.
 */
public interface AnalyzeRoomUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code INSIGHTS_RANGE_TOO_SHORT} si no hay minutos suficientes
     */
    RoomAnalytics execute(AnalyzeRoomQuery query);
}
