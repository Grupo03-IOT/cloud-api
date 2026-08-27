package com.pe.cloudapi.monitoring.application.internal.results;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;

import java.util.Optional;

/**
 * Una sala junto a su última lectura, si tiene alguna.
 *
 * <p>Existe para que los casos de uso devuelvan de una vez lo que hace falta
 * para responder, en vez de que sea el controlador quien vaya juntando piezas.
 * Un controlador que orquesta deja de ser un traductor de HTTP.
 *
 * @param room   la sala
 * @param latest su lectura más reciente; vacío si nunca ha reportado
 */
public record RoomSnapshot(Room room, Optional<RoomReading> latest) {
}
