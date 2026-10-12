package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Si un Edge sigue vivo, derivado de cuándo llegó su último latido.
 */
public enum GatewayStatus {

    ONLINE,

    /** Ha faltado algún latido, pero todavía no los suficientes para darlo por caído. */
    STALE,

    OFFLINE;

    public String toCode() {
        return name().toLowerCase();
    }
}
