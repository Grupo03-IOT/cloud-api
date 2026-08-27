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

    /**
     * Crea un umbral a partir de la configuración indicada.
     *
     * @param command valores de aviso, crítico y minutos sostenidos
     */
    public Threshold(ConfigureThresholdCommand command) {
        this.id = null;
        this.roomTypeId = command.roomTypeId();
        this.metric = command.metric();
        this.warnValue = command.warnValue();
        this.criticalValue = command.criticalValue();
        this.sustainedMinutes = command.sustainedMinutes();
        this.enabled = command.enabled();
    }

    /**
     * Reajusta el umbral. La métrica y el tipo de sala no cambian: si hay que
     * cambiarlos, es otro umbral distinto.
     *
     * @param command nuevos valores
     */
    public void handle(ConfigureThresholdCommand command) {
        this.warnValue = command.warnValue();
        this.criticalValue = command.criticalValue();
        this.sustainedMinutes = command.sustainedMinutes();
        this.enabled = command.enabled();
    }

    /**
     * Indica si una medida supera el valor de aviso.
     *
     * <p>Superarlo en un instante no basta para abrir una alerta: hay que
     * mantenerlo durante {@link #getSustainedMinutes()} minutos. Esa
     * comprobación temporal no vive aquí, porque un umbral solo conoce su
     * propio valor y no la serie histórica.
     *
     * @param value medida a comprobar; {@code null} nunca incumple
     * @return {@code true} si el umbral está activo y la medida lo supera
     */
    public boolean isBreachedBy(Float value) {
        return enabled && value != null && value > warnValue;
    }

    /**
     * Indica si una medida supera el valor crítico, que es el escalón por
     * encima del de aviso.
     *
     * @param value medida a comprobar
     * @return {@code false} si no se ha configurado valor crítico
     */
    public boolean isCriticalFor(Float value) {
        return enabled && value != null && criticalValue != null && value > criticalValue;
    }
}
