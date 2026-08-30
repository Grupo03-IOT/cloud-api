package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;

import java.util.List;

/**
 * Puerto de entrada: listar las salas que aparecieron solas y siguen sin
 * clasificar.
 */
public interface ListUnclassifiedRoomsUseCase {

    List<RoomSnapshot> execute();
}
