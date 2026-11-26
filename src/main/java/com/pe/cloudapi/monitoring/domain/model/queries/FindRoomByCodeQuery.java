package com.pe.cloudapi.monitoring.domain.model.queries;

import java.util.Objects;

/**
 * Una sala por el código que maneja el Edge.
 *
 * @param siteCode local; puede faltar si solo hay uno registrado
 * @param roomCode código de la sala ({@code sala-03})
 */
public record FindRoomByCodeQuery(String siteCode, String roomCode) {

    public FindRoomByCodeQuery {
        Objects.requireNonNull(roomCode, "roomCode");
    }
}
