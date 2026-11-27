package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Lo que envía el miembro. Quién reporta no viaja aquí: sale del token.
 */
@Schema(description = "Discomfort report filed by a member")
public record FileDiscomfortReportResource(

        @NotNull
        @Schema(description = "Room id")
        UUID roomId,

        @NotBlank
        @Schema(description = "NOISE, WARM or COLD", example = "WARM")
        String category,

        @Size(max = 500, message = "MONITORING_DISCOMFORT_COMMENT_TOO_LONG")
        @Schema(description = "Optional, up to 500 characters", example = "Hace calor cerca de la ventana")
        String comment
) {}
