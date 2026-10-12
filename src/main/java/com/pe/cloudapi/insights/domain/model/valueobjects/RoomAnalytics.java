package com.pe.cloudapi.insights.domain.model.valueobjects;

import java.util.List;
import java.util.UUID;

/**
 * Lo que se puede afirmar de una sala en un periodo.
 *
 * @param findings          solo lo concluyente; vacía si no hay nada que contar
 * @param noiseVsOccupancy  si el ruido sube con la gente o es del ambiente
 * @param thermalDrift      cuánto se calienta o se enfría la sala por hora
 * @param indoorVsOutdoor   si la sala sigue a la calle: aislamiento térmico
 * @param noiseDistribution nula si el periodo no tiene lecturas de ruido
 * @param comparison        nula si el periodo anterior no tiene lecturas de ruido
 */
public record RoomAnalytics(
        UUID roomId,
        PeriodWindow window,
        DataQuality dataQuality,
        List<Finding> findings,
        Measure noiseVsOccupancy,
        Measure thermalDrift,
        Measure indoorVsOutdoor,
        NoiseEpisodes noiseEpisodes,
        NoiseDistribution noiseDistribution,
        Comparison comparison
) {

    public RoomAnalytics withComparison(Comparison comparison) {
        return new RoomAnalytics(roomId, window, dataQuality, findings, noiseVsOccupancy,
                thermalDrift, indoorVsOutdoor, noiseEpisodes, noiseDistribution, comparison);
    }
}
