package com.pe.cloudapi.alerting.domain.model.commands;

import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.model.valueobjects.ThresholdMetric;

import java.util.UUID;

/**
 * Crea o ajusta un umbral de un tipo de sala. Es el comando que respalda la
 * pantalla de configuración del administrador.
 */
public record ConfigureThresholdCommand(
        UUID roomTypeId,
        ThresholdMetric metric,
        float warnValue,
        Float criticalValue,
        int sustainedMinutes,
        boolean enabled
) {

    public ConfigureThresholdCommand {
        if (criticalValue != null && warnValue >= criticalValue) {
            throw AlertingError.INVALID_THRESHOLD_RANGE.with(warnValue, criticalValue);
        }
    }
}
