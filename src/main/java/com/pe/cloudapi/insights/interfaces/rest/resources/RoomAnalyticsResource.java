package com.pe.cloudapi.insights.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Conclusiones sobre una sala en un periodo.
 */
@Schema(description = "Statistical findings for a room over a period")
public record RoomAnalyticsResource(

        UUID roomId,
        OffsetDateTime from,
        OffsetDateTime to,

        @Schema(description = "Minutos con datos en el periodo")
        int sampleSize,

        @Schema(description = "Si el ruido sube con la ocupación o es del ambiente")
        CorrelationResource noiseVsOccupancy,

        @Schema(description = "Cuánto se calienta la sala por hora")
        TrendResource thermalDrift,

        @Schema(description = "Minutos con picos de ruido fuera de lo normal para esta sala")
        List<OffsetDateTime> noiseAnomalies

) {

    /**
     * Una correlación con el respaldo que tiene.
     *
     * <p>{@code reliable} en falso significa que el coeficiente existe pero no
     * se sostiene: viene de muy pocas muestras y no debe usarse para decidir
     * nada.
     */
    @Schema(description = "Pearson correlation with its backing")
    public record CorrelationResource(
            @Schema(description = "De -1 a +1; nulo si faltan datos") Double coefficient,
            @Schema(example = "moderate") String strength,
            int sampleSize,
            boolean reliable
    ) {}

    /**
     * Una tendencia ajustada por mínimos cuadrados.
     *
     * @param slopePerHour cuánto cambia la magnitud por hora
     * @param rSquared     qué parte de la variación explica la recta, de 0 a 1
     */
    @Schema(description = "Least-squares trend")
    public record TrendResource(
            @Schema(example = "0.42") Double slopePerHour,
            Double rSquared,
            int sampleSize,
            boolean reliable
    ) {}
}
