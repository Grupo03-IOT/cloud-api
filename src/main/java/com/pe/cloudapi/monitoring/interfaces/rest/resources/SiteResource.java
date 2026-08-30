package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Un local.
 */
@Schema(description = "Site")
public record SiteResource(
        UUID id,
        String code,
        String name,
        String address,

        @Schema(description = "Zona horaria del local. La telemetría viaja en UTC; "
                + "esta es la que usa el negocio para leerla")
        String timezone
) {}
