package com.pe.cloudapi.insights.domain.ports.out;

import com.pe.cloudapi.insights.domain.model.valueobjects.AnalyzedRoom;

import java.util.List;
import java.util.UUID;

/**
 * Qué salas hay, de qué tipo son y en qué zona horaria vive su local.
 */
public interface RoomCatalog {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe
     */
    AnalyzedRoom room(UUID roomId);

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_SITE_NOT_FOUND} si el local no existe
     */
    List<AnalyzedRoom> roomsOfSite(UUID siteId);
}
