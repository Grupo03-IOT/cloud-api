package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * El latido que manda el Edge cada minuto, tenga o no lecturas que subir.
 */
@Schema(description = "Heartbeat the Edge sends every minute")
public record HeartbeatResource(
        @NotBlank @Size(max = 64)
        @Schema(example = "edge-lima-centro") String code,
        @NotBlank @Size(max = 64)
        @Schema(example = "coworking-lima-centro") String siteCode,
        @Size(max = 32)
        @Schema(example = "0.1.0") String version,
        @PositiveOrZero
        @Schema(description = "Minutos en cola sin subir") int pendingUpload,
        @PositiveOrZero
        @Schema(description = "Salas que atiende") int rooms
) {}
