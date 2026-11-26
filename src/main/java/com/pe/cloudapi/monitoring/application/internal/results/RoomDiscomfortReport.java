package com.pe.cloudapi.monitoring.application.internal.results;

import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;

/**
 * Un reporte con el código actual de su sala.
 *
 * @param report   el reporte
 * @param roomCode código de la sala
 */
public record RoomDiscomfortReport(DiscomfortReport report, String roomCode) {
}
