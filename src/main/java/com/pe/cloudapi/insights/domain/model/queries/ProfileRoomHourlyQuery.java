package com.pe.cloudapi.insights.domain.model.queries;

import com.pe.cloudapi.insights.domain.model.valueobjects.Period;

import java.util.Objects;
import java.util.UUID;

/**
 * Perfil hora a hora de una sala en un periodo.
 */
public record ProfileRoomHourlyQuery(UUID roomId, Period period) {

    public ProfileRoomHourlyQuery {
        Objects.requireNonNull(roomId, "roomId");
        Objects.requireNonNull(period, "period");
    }
}
