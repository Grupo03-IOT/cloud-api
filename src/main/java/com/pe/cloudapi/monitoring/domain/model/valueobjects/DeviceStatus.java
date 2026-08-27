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

    /**
     * Representación persistida, en minúsculas.
     *
     * @return el nombre de la constante en minúsculas
     */
    public String toCode() {
        return name().toLowerCase();
    }

    /**
     * Reconstruye el estado desde su representación persistida.
     *
     * <p>Tolera valores desconocidos en vez de fallar: la base no tiene
     * restricción {@code CHECK} sobre esta columna, así que puede contener
     * cualquier texto.
     *
     * @param code texto guardado; puede ser nulo
     * @return el estado correspondiente, o {@link #UNKNOWN} si no se reconoce
     */
    public static DeviceStatus fromCode(String code) {
        if (code == null) return UNKNOWN;
        try {
            return valueOf(code.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}
