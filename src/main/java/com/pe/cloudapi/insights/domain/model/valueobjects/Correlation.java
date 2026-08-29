package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Correlación de Pearson entre dos series, con el tamaño de la muestra.
 *
 * <p>El coeficiente por sí solo engaña: con tres puntos casi cualquier par de
 * series correlaciona. Por eso {@code sampleSize} viaja con él y quien lo
 * interpreta puede descartarlo si es pequeño.
 *
 * @param coefficient de -1 a +1; nulo si no había datos suficientes
 * @param sampleSize  minutos usados para calcularlo
 */
public record Correlation(Double coefficient, int sampleSize) {

    /** Bajo esta cantidad de muestras el coeficiente no es interpretable. */
    private static final int MINIMUM_SAMPLE = 30;

    public static Correlation insufficientData(int sampleSize) {
        return new Correlation(null, sampleSize);
    }

    public boolean isReliable() {
        return coefficient != null && sampleSize >= MINIMUM_SAMPLE;
    }

    /**
     * Etiqueta legible de la fuerza de la relación, según los umbrales
     * convencionales en ciencias sociales.
     */
    public String strength() {
        if (!isReliable()) {
            return "insufficient_data";
        }
        double magnitude = Math.abs(coefficient);
        if (magnitude < 0.3) return "negligible";
        if (magnitude < 0.5) return "weak";
        if (magnitude < 0.7) return "moderate";
        return "strong";
    }
}
