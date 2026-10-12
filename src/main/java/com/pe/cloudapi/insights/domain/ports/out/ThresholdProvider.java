package com.pe.cloudapi.insights.domain.ports.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Contra qué umbral se mide el cumplimiento de una sala.
 */
public interface ThresholdProvider {

    /**
     * El valor de aviso de {@code laeq} del tipo de sala, en dBA.
     *
     * @return vacío si el tipo no tiene ese umbral, o lo tiene desactivado
     */
    Optional<Float> noiseWarnLevel(UUID roomTypeId);
}
