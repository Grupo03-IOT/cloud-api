package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Recta ajustada por mínimos cuadrados a una serie temporal.
 *
 * <p>La pendiente se expresa <strong>por hora</strong>, no por milisegundo, que
 * es la unidad en la que se calcula: una deriva de 0,000000001 por milisegundo
 * no le dice nada a nadie.
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

    public boolean isReliable() {
        return slopePerHour != null && rSquared != null
                && sampleSize >= MINIMUM_SAMPLE && rSquared >= MINIMUM_FIT;
    }
}
