package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateRoomTypeCommand;

/**
 * Puerto de entrada: dar de alta un tipo de sala.
 */
public interface CreateRoomTypeUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_SITE_NOT_FOUND} si el local no existe, o
     *         {@code MONITORING_ROOM_TYPE_CODE_ALREADY_USED} si el código está tomado
     */
    RoomType execute(CreateRoomTypeCommand command);
}
