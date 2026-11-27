package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Por qué lo que dice el miembro no coincide con lo que marcaba el sensor.
 */
public enum DiscrepancyReason {
    NOISE_WITHIN_RANGE,
    TEMPERATURE_WITHIN_RANGE
}
