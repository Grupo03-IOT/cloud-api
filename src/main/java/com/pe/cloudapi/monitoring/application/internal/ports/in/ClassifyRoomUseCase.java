package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;
import com.pe.cloudapi.monitoring.domain.model.commands.ClassifyRoomCommand;

/**
 * Puerto de entrada: asignar un tipo a una sala.
 *
 * <p>Es lo que saca a una sala de la bandeja de pendientes: hasta que se
 * clasifica no tiene umbrales aplicables.
 */
public interface ClassifyRoomUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND}, {@code MONITORING_ROOM_TYPE_NOT_FOUND}
     *         o {@code MONITORING_ROOM_TYPE_FROM_ANOTHER_SITE}
     */
    RoomSnapshot execute(ClassifyRoomCommand command);
}
