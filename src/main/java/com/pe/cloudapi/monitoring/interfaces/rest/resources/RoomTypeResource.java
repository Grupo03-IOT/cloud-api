package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Un tipo de sala.
 */
@Schema(description = "Room type")
public record RoomTypeResource(
        UUID id,
        UUID siteId,
        String code,
        String displayName,
        String description
) {}
