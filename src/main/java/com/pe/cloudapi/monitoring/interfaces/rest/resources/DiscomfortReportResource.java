package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Un reporte de incomodidad con la lectura de la sala en ese momento.
 *
 * <p>{@code reading} y {@code comment} salen como {@code null}, no se omiten.
 */
@Schema(description = "Discomfort report")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record DiscomfortReportResource(
        UUID id,
        UUID roomId,
        @Schema(example = "sala-03") String roomCode,
        UUID userId,
        @Schema(example = "WARM") String category,
        String comment,
        OffsetDateTime createdAt,
        @Schema(description = "Latest reading of the room when the report was filed; "
                + "null if none was newer than 10 minutes")
        Reading reading,
        @Schema(description = "true when the reading was within the room type threshold "
                + "for the reported category")
        boolean discrepancy,
        @Schema(description = "NOISE_WITHIN_RANGE or TEMPERATURE_WITHIN_RANGE; "
                + "null when there is no discrepancy", example = "TEMPERATURE_WITHIN_RANGE")
        String discrepancyReason
) {

    /** Lectura del momento. Sus campos sueltos sí se omiten si faltan. */
    @Schema(description = "Room reading at report time")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Reading(OffsetDateTime ts, Float laeq, Float tempC, Float rhPct, Float occupiedPct) {
    }
}
