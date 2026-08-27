package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Cuánto del periodo estuvo ocupada la sala. Se deriva del sensor de presencia
 * mmWave, no de una agenda de reservas.
 *
 * @param occupiedPct porcentaje del periodo con presencia detectada
 * @param transitions cambios vacío/ocupado contados en el periodo
 */
public record Occupancy(Float occupiedPct, Integer transitions) {

    /**
     * Sala vacía durante todo el periodo.
     *
     * @return ocupación del 0% sin transiciones
     */
    public static Occupancy vacant() {
        return new Occupancy(0f, 0);
    }

    /**
     * Indica si la sala estuvo ocupada al menos la mitad del periodo.
     *
     * <p>Sirve para decidir si un minuto cuenta como "en uso" al construir
     * sesiones de ocupación, sin exigir presencia continua: alguien que sale
     * treinta segundos a por un café no termina la sesión.
     *
     * @return {@code true} si la ocupación llega al 50%
     */
    public boolean isMostlyOccupied() {
        return occupiedPct != null && occupiedPct >= 50f;
    }
}
