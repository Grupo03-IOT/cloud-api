package com.pe.cloudapi.alerting.application.internal.results;

import com.pe.cloudapi.alerting.domain.model.entities.Threshold;

import java.util.List;

/**
 * Los umbrales que le tocan a una sala concreta.
 *
 * <p>Resueltos por sala y no por tipo a propósito: quien los consume —el Edge—
 * conoce sus salas por código y no sabe que existen los tipos. Traducir la
 * taxonomía es trabajo de quien la posee.
 *
 * @param roomCode   el código con el que el dispositivo reporta
 * @param thresholds vacío si la sala no está clasificada o su tipo no tiene
 *                   ninguno configurado
 */
public record RoomThresholds(String roomCode, List<Threshold> thresholds) {
}
