package com.pe.cloudapi.monitoring.interfaces.acl;

import com.pe.cloudapi.monitoring.application.internal.results.RoomReadings;
import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;

import java.util.List;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * La puerta pública de {@code monitoring} hacia los demás contextos.
 *
 * <p>Este contrato es toda la superficie: lo que no esté declarado aquí, no se expone.
 */
public interface MonitoringContextFacade {

    /**
     * La serie temporal de una sala entre dos instantes, ambos inclusive, con la sala misma.
     *
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe — nunca una lista vacía
     */
    RoomReadings readingsInRange(UUID roomId, OffsetDateTime from, OffsetDateTime to);

    /**
     * Todas las salas conocidas, con su tipo si ya se clasificaron.
     *
     * <p>Las sin clasificar salen igual: quien las consuma decide qué hacer con
     * ellas, y esconderlas haría que una sala recién descubierta pareciera no
     * existir.
     */
    List<RoomSnapshot> rooms();
}
