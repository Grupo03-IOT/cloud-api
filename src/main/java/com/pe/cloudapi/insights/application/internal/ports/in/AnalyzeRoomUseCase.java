package com.pe.cloudapi.insights.application.internal.ports.in;

import com.pe.cloudapi.insights.domain.model.queries.AnalyzeRoomQuery;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomAnalytics;

/**
 * Puerto de entrada: analizar una sala en un periodo.
 */
public interface AnalyzeRoomUseCase {

    /**
     * Un periodo sin lecturas no es un error: sale con cobertura cero y sus
     * medidas sin datos.
     *
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe
     */
    RoomAnalytics execute(AnalyzeRoomQuery query);
}
