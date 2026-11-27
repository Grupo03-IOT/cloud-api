package com.pe.cloudapi.alerting.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * Un evento de alerta del Edge: su apertura y, si ya ocurrió, su cierre.
 */
@Schema(description = "Alert event raised by the Edge")
public record AlertEventResource(

        @NotBlank @Size(max = 120)
        @Schema(description = "Edge code + ':' + local alert id. Idempotency key: "
                + "resending it updates the alert instead of duplicating it",
                example = "edge-lima-centro:42")
        String sourceKey,

        @NotBlank @Size(max = 60)
        @Schema(description = "Room code, as in the readings (not the Cloud id)", example = "sala-03")
        String roomId,

        @NotNull
        @Pattern(regexp = "^[a-z][a-z0-9_]{0,39}$", message = "ALERTING_INVALID_ALERT_RULE")
        @Schema(example = "sustained_noise")
        String rule,

        @NotBlank
        @Schema(description = "info, warning or critical", example = "warning")
        String severity,

        @NotBlank @Size(max = 200)
        @Schema(example = "Noise above 60 dBA for 2 min")
        String message,

        @Schema(description = "Measured value that raised the alert", example = "66.4")
        Double value,

        @NotNull
        @Schema(description = "When the condition started, UTC", example = "2026-11-08T14:10:00Z")
        OffsetDateTime openedAt,

        @Schema(description = "When it went back to normal, UTC; null while still open")
        OffsetDateTime closedAt
) {}
