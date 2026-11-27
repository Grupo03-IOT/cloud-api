package com.pe.cloudapi.alerting.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Una alerta tal como la ven las apps.
 *
 * <p>Los campos vacíos salen como {@code null} y no se omiten: las apps
 * distinguen «sin reconocer» de «campo desconocido».
 */
@Schema(description = "Alert")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record AlertResource(
        UUID id,
        UUID roomId,
        @Schema(example = "sala-03") String roomCode,
        @Schema(example = "sustained_noise") String rule,
        @Schema(example = "warning") String severity,
        String message,
        Double value,
        @Schema(description = "OPEN, ACKNOWLEDGED or CLOSED") String status,
        OffsetDateTime openedAt,
        OffsetDateTime closedAt,
        @Schema(description = "AUTO when the Edge closed it") String closedBy,
        OffsetDateTime acknowledgedAt,
        UUID acknowledgedBy,
        String correctiveAction
) {}
