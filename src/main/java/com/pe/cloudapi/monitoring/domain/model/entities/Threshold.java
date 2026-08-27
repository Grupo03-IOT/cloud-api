package com.pe.cloudapi.monitoring.domain.model.entities;

import com.pe.cloudapi.monitoring.domain.model.commands.ConfigureThresholdCommand;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ThresholdMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Límite configurable que dispara una alerta, expresado como un valor más el
 * tiempo que debe sostenerse: un portazo no es un problema de ruido, veinte
 * minutos de gritos sí.
 *
 * <p>Los umbrales viven en la base de datos y no en el código para que la
 * administración pueda ajustarlos desde la aplicación web sin desplegar.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Threshold {

    private final UUID id;
    private final UUID roomTypeId;
    private final ThresholdMetric metric;
    @Setter private float warnValue;
    @Setter private Float criticalValue;
    @Setter private int sustainedMinutes;
    @Setter private boolean enabled;

    public Threshold(ConfigureThresholdCommand command) {
        this.id = null;
        this.roomTypeId = command.roomTypeId();
        this.metric = command.metric();
        this.warnValue = command.warnValue();
        this.criticalValue = command.criticalValue();
        this.sustainedMinutes = command.sustainedMinutes();
        this.enabled = command.enabled();
    }

    public void handle(ConfigureThresholdCommand command) {
        this.warnValue = command.warnValue();
        this.criticalValue = command.criticalValue();
        this.sustainedMinutes = command.sustainedMinutes();
        this.enabled = command.enabled();
    }

    public boolean isBreachedBy(Float value) {
        return enabled && value != null && value > warnValue;
    }

    public boolean isCriticalFor(Float value) {
        return enabled && value != null && criticalValue != null && value > criticalValue;
    }
}
