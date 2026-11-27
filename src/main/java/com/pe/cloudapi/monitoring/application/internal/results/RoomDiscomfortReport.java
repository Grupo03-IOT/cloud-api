package com.pe.cloudapi.monitoring.application.internal.results;

import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscrepancyReason;

/**
 * Un reporte con el código actual de su sala y su discrepancia con la medición.
 *
 * @param report            el reporte
 * @param roomCode          código de la sala
 * @param discrepancyReason {@code null} si no hay discrepancia
 */
public record RoomDiscomfortReport(DiscomfortReport report, String roomCode,
                                   DiscrepancyReason discrepancyReason) {

    public boolean hasDiscrepancy() {
        return discrepancyReason != null;
    }
}
