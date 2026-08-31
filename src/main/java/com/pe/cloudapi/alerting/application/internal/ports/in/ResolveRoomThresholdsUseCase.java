package com.pe.cloudapi.alerting.application.internal.ports.in;

import com.pe.cloudapi.alerting.application.internal.results.RoomThresholds;

import java.util.List;

/**
 * Puerto de entrada: qué umbral se le aplica a cada sala.
 *
 * <p>Lo consume el Edge, que evalúa las alertas donde están los datos y donde
 * está el actuador. El cloud decide la política; el Edge la aplica.
 */
public interface ResolveRoomThresholdsUseCase {

    List<RoomThresholds> execute();
}
