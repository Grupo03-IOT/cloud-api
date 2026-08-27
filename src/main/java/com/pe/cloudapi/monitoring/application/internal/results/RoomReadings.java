package com.pe.cloudapi.monitoring.application.internal.results;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;

import java.util.List;

/**
 * Una serie de lecturas junto a la sala a la que pertenecen.
 *
 * <p>Se devuelve la sala además de las lecturas porque quien responde necesita
 * su código: el agregado la referencia por identificador, y el cliente la
 * conoce por el código que lleva el firmware.
 *
 * @param room     la sala consultada
 * @param readings sus lecturas, ordenadas de más antigua a más reciente
 */
public record RoomReadings(Room room, List<RoomReading> readings) {
}
