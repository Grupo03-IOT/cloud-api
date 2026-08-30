package com.pe.cloudapi.alerting.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Un umbral configurado.
 */
@Schema(description = "Configured threshold")
public record ThresholdResource(
        UUID id,
        UUID roomTypeId,
        @Schema(example = "laeq") String metric,
        Float warnValue,
        Float criticalValue,
        Integer sustainedMinutes,
        Boolean enabled
) {}
