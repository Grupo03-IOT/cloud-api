package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Correlación de Pearson entre dos series, con el tamaño de la muestra.
 *
 * @param coefficient de -1 a +1; nulo si no había datos suficientes
 * @param sampleSize  minutos o pares usados para calcularlo
 */
public record Correlation(Double coefficient, int sampleSize) {

    /** Bajo esta cantidad de muestras el coeficiente no es interpretable. */
    private static final int MINIMUM_SAMPLE = 30;

    public static Correlation insufficientData(int sampleSize) {
        return new Correlation(null, sampleSize);
    }

    public Verdict verdict() {
        if (sampleSize == 0) {
            return Verdict.NO_DATA;
        }
        if (coefficient == null || sampleSize < MINIMUM_SAMPLE) {
            return Verdict.NOT_ENOUGH_EVIDENCE;
        }
        return Verdict.CONCLUSIVE;
    }

    /** Nula si no hay coeficiente. */
    public Direction direction() {
        if (coefficient == null) {
            return null;
        }
        return coefficient >= 0 ? Direction.POSITIVE : Direction.NEGATIVE;
    }
}
