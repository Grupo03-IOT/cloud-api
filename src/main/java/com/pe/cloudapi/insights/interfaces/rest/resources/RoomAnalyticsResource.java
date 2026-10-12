package com.pe.cloudapi.insights.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Conclusiones sobre una sala en un periodo.
 *
 * <p>Un campo ausente no es un cero: falta porque no hay respaldo para darlo.
 */
@Schema(description = "What can be said about a room over a period")
public record RoomAnalyticsResource(
        UUID roomId,
        @Schema(example = "2026-10") String period,
        @Schema(description = "Inicio del periodo en la zona del local, inclusive") OffsetDateTime from,
        @Schema(description = "Fin del periodo en la zona del local, exclusive") OffsetDateTime to,
        @Schema(example = "America/Lima") String timezone,
        DataQualityResource dataQuality,
        @Schema(description = "Solo lo concluyente. Vacía si no hay nada que contar, que es un buen resultado")
        List<FindingResource> findings,
        MeasuresResource measures,
        NoiseEpisodesResource noiseEpisodes,
        @Schema(description = "No viene si el periodo no tiene lecturas de ruido")
        NoiseDistributionResource noiseDistribution,
        @Schema(description = "No viene si el periodo anterior no tiene lecturas de ruido")
        ComparisonResource comparison
) {

    @Schema(description = "How much of the period has readings")
    public record DataQualityResource(
            @Schema(description = "Minutos del periodo completo, aunque no haya terminado")
            long expectedMinutes,
            int receivedMinutes,
            @Schema(example = "95.3") double coveragePct
    ) {}

    @Schema(description = "A conclusion worth telling. The client translates the code")
    public record FindingResource(
            @Schema(example = "noise_follows_occupancy") String code,
            double value,
            @Schema(example = "pearson_r") String unit,
            @Schema(example = "positive") String direction
    ) {}

    @Schema(description = "The three calculations, with their backing")
    public record MeasuresResource(
            MeasureResource noiseVsOccupancy,
            MeasureResource thermalDrift,
            MeasureResource indoorVsOutdoor
    ) {}

    @Schema(description = "A calculation. Without a conclusive verdict there is no code, value or direction")
    public record MeasureResource(
            @Schema(example = "conclusive", allowableValues = {"conclusive", "not_enough_evidence", "no_data"})
            String verdict,
            @Schema(example = "noise_follows_occupancy") String code,
            Double value,
            @Schema(example = "pearson_r") String unit,
            @Schema(example = "positive", allowableValues = {"positive", "negative", "rising", "falling"})
            String direction,
            ConfidenceResource confidence
    ) {}

    @Schema(description = "What the value rests on")
    public record ConfidenceResource(int sampleSize, Double rSquared) {}

    @Schema(description = "Anomalous noise, grouped into episodes")
    public record NoiseEpisodesResource(
            @Schema(description = "Todos los episodios, aunque worst venga recortada") int total,
            @Schema(description = "Los de mayor pico, del más fuerte al más débil") List<NoiseEpisodeResource> worst
    ) {}

    @Schema(description = "Consecutive anomalous minutes")
    public record NoiseEpisodeResource(
            OffsetDateTime from,
            @Schema(description = "Último minuto, inclusive") OffsetDateTime to,
            int minutes,
            float peakLaeq,
            OffsetDateTime peakAt
    ) {}

    @Schema(description = "Noise percentiles and, with a threshold, time above it")
    public record NoiseDistributionResource(
            double p50,
            double p90,
            double p95,
            int sampleSize,
            @Schema(description = "Umbral de aviso de laeq del tipo de sala") Float threshold,
            Integer minutesOver,
            @Schema(example = "17.9") Double pctOver
    ) {}

    @Schema(description = "This period against the previous one. Negative is better")
    public record ComparisonResource(
            @Schema(example = "2026-09") String previousPeriod,
            @Schema(description = "Puntos porcentuales; no viene si no hay umbral") Double pctOverDelta,
            double p90Delta
    ) {}
}
