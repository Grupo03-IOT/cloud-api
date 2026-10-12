package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.RoomContext;
import com.pe.cloudapi.monitoring.domain.model.queries.GetRoomQuery;

/**
 * Puerto de entrada: una sala con su local y su tipo.
 */
public interface GetRoomContextUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe
     */
    RoomContext execute(GetRoomQuery query);
}
