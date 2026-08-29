package com.pe.cloudapi.insights.domain.model.results;

import com.pe.cloudapi.insights.domain.model.valueobjects.Correlation;
import com.pe.cloudapi.insights.domain.model.valueobjects.Trend;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lo que se puede afirmar de una sala en un periodo.
 *
 * @param roomId            sala analizada
 * @param from              inicio del periodo
 * @param to                fin del periodo
 * @param sampleSize        minutos con datos
 * @param noiseVsOccupancy  si el ruido sube con la gente o es del ambiente
 * @param thermalDrift      cuánto se calienta la sala por hora
 * @param noiseAnomalies    minutos con picos fuera de lo normal
 */
public record RoomAnalytics(
        UUID roomId,
        OffsetDateTime from,
        OffsetDateTime to,
        int sampleSize,
        Correlation noiseVsOccupancy,
        Trend thermalDrift,
        List<OffsetDateTime> noiseAnomalies
) {}
