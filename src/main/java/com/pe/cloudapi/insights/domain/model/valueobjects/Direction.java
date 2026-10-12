package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Hacia dónde va una relación o una tendencia.
 */
public enum Direction {

    POSITIVE,
    NEGATIVE,
    RISING,
    FALLING;

    public String toCode() {
        return name().toLowerCase();
    }
}
