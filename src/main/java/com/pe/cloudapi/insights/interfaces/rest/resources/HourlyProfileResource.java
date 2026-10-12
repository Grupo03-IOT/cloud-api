package com.pe.cloudapi.insights.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Las 24 horas del día de una sala a lo largo de un periodo.
 */
@Schema(description = "A room hour by hour, in the site's timezone")
public record HourlyProfileResource(
        UUID roomId,
        @Schema(example = "2026-10") String period,
        OffsetDateTime from,
        OffsetDateTime to,
        @Schema(example = "America/Lima") String timezone,
        @Schema(description = "Siempre 24, de las 0 a las 23") List<HourSlotResource> hours
) {

    @Schema(description = "One hour of the day. Without a conclusive verdict there are no values")
    public record HourSlotResource(
            int hour,
            @Schema(example = "conclusive") String verdict,
            int sampleSize,
            @Schema(description = "Promediado por energía") Double avgLaeq,
            Double p90Laeq,
            Double avgOccupiedPct
    ) {}
}
