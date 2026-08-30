package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.RoomReadings;
import com.pe.cloudapi.monitoring.domain.model.queries.GetLatestReadingQuery;

/**
 * Puerto de entrada: obtener la lectura más reciente de una sala.
 */
public interface GetLatestReadingUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe, o
     *         {@code MONITORING_ROOM_HAS_NO_READINGS} si aún no ha reportado
     */
    RoomReadings execute(GetLatestReadingQuery query);
}
