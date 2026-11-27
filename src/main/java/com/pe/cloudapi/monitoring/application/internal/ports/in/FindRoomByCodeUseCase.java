package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.queries.FindRoomByCodeQuery;

/**
 * Puerto de entrada: resolver una sala por su código, sin darla de alta.
 */
public interface FindRoomByCodeUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_SITE_NOT_FOUND}, {@code MONITORING_NO_SITE_AVAILABLE},
     *         {@code MONITORING_SITE_CODE_REQUIRED} o {@code MONITORING_ROOM_NOT_FOUND}
     */
    Room execute(FindRoomByCodeQuery query);
}
