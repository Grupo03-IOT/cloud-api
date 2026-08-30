package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;

import java.util.List;

/**
 * Puerto de entrada: listar las salas activas con su última lectura.
 */
public interface ListRoomsUseCase {

    List<RoomSnapshot> execute();
}
