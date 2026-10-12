package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Un Edge y si sigue vivo.
 */
@Schema(description = "An Edge and whether it is alive")
public record GatewayResource(
        UUID id,
        @Schema(example = "edge-lima-centro") String code,
        UUID siteId,
        @Schema(example = "0.1.0") String version,
        OffsetDateTime lastSeen,
        @Schema(description = "Si crece mientras llegan latidos, el cloud le rechaza los lotes")
        int pendingUpload,
        int roomsReported,
        @Schema(example = "online", allowableValues = {"online", "stale", "offline"}) String status
) {}
