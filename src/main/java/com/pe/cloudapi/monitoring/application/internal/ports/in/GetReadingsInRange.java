package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.RoomReadings;
import com.pe.cloudapi.monitoring.domain.model.queries.GetReadingsInRangeQuery;

/**
 * Puerto de entrada: obtener la serie temporal de una sala.
 */
public interface GetReadingsInRange {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe
     */
    RoomReadings execute(GetReadingsInRangeQuery query);
}
