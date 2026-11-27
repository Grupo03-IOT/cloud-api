package com.pe.cloudapi.alerting.domain.model.valueobjects;

import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;

/**
 * Ciclo de vida de una alerta: {@code OPEN → ACKNOWLEDGED → CLOSED}, o
 * {@code OPEN → CLOSED} si se normaliza antes de que alguien la atienda.
 *
 * <p>No hay vuelta atrás: si la condición reaparece, nace otra alerta.
 */
public enum AlertStatus {
    OPEN,
    ACKNOWLEDGED,
    CLOSED;

    /**
     * @param code texto recibido o persistido, sin distinguir mayúsculas
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code ALERTING_UNKNOWN_ALERT_STATUS} si no es ninguno de los declarados
     */
    public static AlertStatus fromCode(String code) {
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw AlertingError.UNKNOWN_ALERT_STATUS.with(code);
        }
    }
}
