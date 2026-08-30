package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;
import com.pe.cloudapi.monitoring.domain.model.queries.GetRoomQuery;

/**
 * Puerto de entrada: obtener una sala con su última lectura.
 */
public interface GetRoomUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe
     */
    RoomSnapshot execute(GetRoomQuery query);
}
