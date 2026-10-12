package com.pe.cloudapi.insights.application.internal.ports.in;

import com.pe.cloudapi.insights.domain.model.queries.ProfileRoomHourlyQuery;
import com.pe.cloudapi.insights.domain.model.valueobjects.HourlyProfile;

/**
 * Puerto de entrada: el perfil hora a hora de una sala en un periodo.
 */
public interface ProfileRoomHourlyUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe
     */
    HourlyProfile execute(ProfileRoomHourlyQuery query);
}
