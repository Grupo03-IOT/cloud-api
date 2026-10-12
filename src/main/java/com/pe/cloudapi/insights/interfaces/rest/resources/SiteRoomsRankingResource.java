package com.pe.cloudapi.insights.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Las salas de un local, de peor a mejor.
 */
@Schema(description = "The rooms of a site, worst first")
public record SiteRoomsRankingResource(
        UUID siteId,
        @Schema(example = "2026-10") String period,
        List<RoomRankingResource> rooms
) {

    @Schema(description = "A room in its site's ranking")
    public record RoomRankingResource(
            UUID roomId,
            @Schema(example = "sala-03") String code,
            @Schema(example = "phone_booth") String roomType,
            RoomAnalyticsResource.DataQualityResource dataQuality,
            @Schema(description = "No viene si la sala no tiene lecturas de ruido en el periodo")
            RoomAnalyticsResource.NoiseDistributionResource noiseDistribution
    ) {}
}
