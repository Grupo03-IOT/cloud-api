package com.pe.cloudapi.alerting.domain.model.valueobjects;

/**
 * Quién cerró la alerta.
 *
 * <p>{@code AUTO} cuando el Edge informa que la condición se normalizó.
 * {@code MANUAL} queda reservado para un cierre desde la interfaz, que aún no existe.
 */
public enum AlertClosure {
    AUTO,
    MANUAL
}
