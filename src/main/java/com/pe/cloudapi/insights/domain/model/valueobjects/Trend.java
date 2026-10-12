package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Recta ajustada por mínimos cuadrados a una serie temporal.
 *
 * @param slopePerHour cuánto cambia la magnitud por cada hora
 * @param rSquared     qué parte de la variación explica la recta, de 0 a 1
 * @param sampleSize   minutos usados
 */
public record Trend(Double slopePerHour, Double rSquared, int sampleSize) {

    private static final int MINIMUM_SAMPLE = 30;

    /** Por debajo de esto la recta no explica lo suficiente como para fiarse. */
    private static final double MINIMUM_FIT = 0.5;

    public static Trend insufficientData(int sampleSize) {
        return new Trend(null, null, sampleSize);
    }

    public Verdict verdict() {
        if (sampleSize == 0) {
            return Verdict.NO_DATA;
        }
        if (slopePerHour == null || rSquared == null
                || sampleSize < MINIMUM_SAMPLE || rSquared < MINIMUM_FIT) {
            return Verdict.NOT_ENOUGH_EVIDENCE;
        }
        return Verdict.CONCLUSIVE;
    }

    /** Nula si no hay pendiente. */
    public Direction direction() {
        if (slopePerHour == null) {
            return null;
        }
        return slopePerHour >= 0 ? Direction.RISING : Direction.FALLING;
    }
}
