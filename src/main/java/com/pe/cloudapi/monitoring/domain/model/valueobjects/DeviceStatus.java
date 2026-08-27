package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Estado de conectividad de un dispositivo, derivado de hace cuánto reportó.
 *
 * <p>Se persiste en minúsculas, así que la conversión pasa por
 * {@link #toCode()} y {@link #fromCode(String)} en vez de {@code valueOf}.
 */
public enum DeviceStatus {
    UNKNOWN,
    ONLINE,
    STALE,
    OFFLINE;

    public String toCode() {
        return name().toLowerCase();
    }

    public static DeviceStatus fromCode(String code) {
        if (code == null) return UNKNOWN;
        try {
            return valueOf(code.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}
