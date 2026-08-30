package com.pe.cloudapi.alerting.interfaces.rest.transform;

import com.pe.cloudapi.alerting.domain.model.commands.ConfigureThresholdCommand;
import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.valueobjects.ThresholdMetric;
import com.pe.cloudapi.alerting.interfaces.rest.resources.ConfigureThresholdResource;
import com.pe.cloudapi.alerting.interfaces.rest.resources.ThresholdResource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Traduce entre los recursos REST de umbrales y el dominio.
 */
@Component
public class ThresholdResourceAssembler {

    /**
     * @param roomTypeId tipo de sala, que viene en la ruta
     * @param metric     métrica, que viene en la ruta
     * @param resource   cuerpo de la petición
     */
    public ConfigureThresholdCommand toCommand(UUID roomTypeId, String metric,
                                               ConfigureThresholdResource resource) {
        return new ConfigureThresholdCommand(
                roomTypeId,
                ThresholdMetric.fromCode(metric),
                resource.warnValue(),
                resource.criticalValue(),
                resource.sustainedMinutes(),
                resource.enabled());
    }

    public ThresholdResource toResource(Threshold threshold) {
        return new ThresholdResource(
                threshold.getId(),
                threshold.getRoomTypeId(),
                threshold.getMetric().toCode(),
                threshold.getWarnValue(),
                threshold.getCriticalValue(),
                threshold.getSustainedMinutes(),
                threshold.isEnabled());
    }
}
