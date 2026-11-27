package com.pe.cloudapi.alerting.domain.ports.out;

import java.util.Map;
import java.util.UUID;

/**
 * Lo que {@code alerting} necesita saber de las salas para guardar y mostrar alertas.
 *
 * <p>El Edge conoce las salas por su código; aquí se guarda el id real, para
 * que renombrar el código en Cloud no deje alertas huérfanas.
 */
public interface RoomDirectory {

    /**
     * @param siteCode local; puede faltar si solo hay uno registrado
     * @param roomCode código de sala que maneja el Edge
     * @return el id real de la sala
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe
     */
    UUID roomIdOf(String siteCode, String roomCode);

    /**
     * @return el código de cada sala conocida, por id
     */
    Map<UUID, String> roomCodes();
}
