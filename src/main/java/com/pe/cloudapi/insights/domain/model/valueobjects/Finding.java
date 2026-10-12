package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Una conclusión que merece contarse.
 *
 * @param code      clave estable, que el cliente traduce e interpola con {@code value}
 * @param direction nula cuando la conclusión no tiene sentido
 */
public record Finding(String code, double value, String unit, Direction direction) {

    public static Finding of(Measure measure) {
        return new Finding(measure.code(), measure.value(), measure.unit(), measure.direction());
    }
}
