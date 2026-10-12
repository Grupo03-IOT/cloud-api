package com.pe.cloudapi.monitoring.application.internal.results;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;

import java.util.Optional;

/**
 * Una sala con su local y su tipo.
 *
 * @param room     la sala
 * @param site     el local al que pertenece, que fija su zona horaria
 * @param roomType su tipo; vacío mientras no esté clasificada
 */
public record RoomContext(Room room, Site site, Optional<RoomType> roomType) {
}
