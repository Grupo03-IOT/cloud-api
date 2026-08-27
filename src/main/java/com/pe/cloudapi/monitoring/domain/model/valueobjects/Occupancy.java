package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Cuánto del periodo estuvo ocupada la sala. Se deriva del sensor de presencia
 * mmWave, no de una agenda de reservas.
 *
 * @param occupiedPct porcentaje del periodo con presencia detectada
 * @param transitions cambios vacío/ocupado contados en el periodo
 */
public record Occupancy(Float occupiedPct, Integer transitions) {

    public static Occupancy vacant() {
        return new Occupancy(0f, 0);
    }

    public boolean isMostlyOccupied() {
        return occupiedPct != null && occupiedPct >= 50f;
    }
}
