package com.pe.cloudapi.alerting.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param correctiveAction lo que se hizo para atender la alerta
 */
@Schema(description = "Acknowledgement of an open alert")
public record AcknowledgeAlertResource(

        @NotBlank(message = "ALERTING_CORRECTIVE_ACTION_REQUIRED")
        @Size(min = 3, max = 500, message = "ALERTING_CORRECTIVE_ACTION_REQUIRED")
        @Schema(description = "Corrective action taken, 3 to 500 characters",
                example = "Asked for silence in room 3")
        String correctiveAction
) {}
