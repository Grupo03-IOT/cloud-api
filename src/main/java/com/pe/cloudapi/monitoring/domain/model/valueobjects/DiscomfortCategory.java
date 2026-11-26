package com.pe.cloudapi.monitoring.domain.model.valueobjects;

import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;

/**
 * Qué molesta al miembro que reporta.
 */
public enum DiscomfortCategory {
    NOISE,
    WARM,
    COLD;

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_UNKNOWN_DISCOMFORT_CATEGORY} si no es ninguna de las declaradas
     */
    public static DiscomfortCategory fromCode(String code) {
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw MonitoringError.UNKNOWN_DISCOMFORT_CATEGORY.with(code);
        }
    }
}
