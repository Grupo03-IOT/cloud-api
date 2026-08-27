package com.pe.cloudapi.monitoring.domain.model.commands;

import java.util.UUID;

/**
 * Da de alta una sala.
 *
 * <p>{@link #autoRegistered(java.util.UUID, String)} cubre el caso en que la
 * sala aparece sola porque un dispositivo desconocido reportó por ella: se usa
 * el propio código como nombre visible hasta que un administrador la edite.
 */
public record CreateRoomCommand(
        UUID siteId,
        String code,
        String displayName
) {
    /**
     * Alta de una sala que aparece sola porque un dispositivo desconocido
     * reportó por ella. Se usa el propio código como nombre visible hasta que
     * un administrador la edite.
     *
     * @param siteId local al que se engancha
     * @param code   código que venía en el lote
     * @return el comando de alta
     */
    public static CreateRoomCommand autoRegistered(UUID siteId, String code) {
        return new CreateRoomCommand(siteId, code, code);
    }
}
