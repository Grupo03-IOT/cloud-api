package com.pe.cloudapi.alerting.domain.model.valueobjects;

import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;

/**
 * Gravedad que declara el Edge al abrir la alerta.
 *
 * <p>Viaja y se persiste en minúsculas ({@code warning}), igual que la emite el Edge.
 */
public enum AlertSeverity {
    INFO,
    WARNING,
    CRITICAL;

    public String toCode() {
        return name().toLowerCase();
    }

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code ALERTING_UNKNOWN_ALERT_SEVERITY} si no es ninguna de las declaradas
     */
    public static AlertSeverity fromCode(String code) {
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw AlertingError.UNKNOWN_ALERT_SEVERITY.with(code);
        }
    }
}
