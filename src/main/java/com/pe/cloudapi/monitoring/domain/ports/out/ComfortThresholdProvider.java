package com.pe.cloudapi.monitoring.domain.ports.out;

import com.pe.cloudapi.monitoring.domain.model.valueobjects.ComfortThresholds;

import java.util.UUID;

/**
 * Contra qué límites se contrasta lo que reporta un miembro.
 */
public interface ComfortThresholdProvider {

    /**
     * @return los límites de los umbrales activos del tipo de sala; sin límite
     *         en las métricas que no tienen umbral
     */
    ComfortThresholds comfortThresholdsOf(UUID roomTypeId);
}
