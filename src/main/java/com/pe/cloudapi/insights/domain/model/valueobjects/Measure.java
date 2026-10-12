package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Un cálculo con su veredicto.
 *
 * <p>Sin veredicto {@link Verdict#CONCLUSIVE} no hay {@code code}, {@code value}
 * ni {@code direction}.
 *
 * @param code       clave estable de la conclusión, que el cliente traduce
 * @param unit       {@code pearson_r} o {@code degc_per_hour}
 * @param sampleSize muestras o pares usados
 * @param rSquared   solo en las tendencias
 */
public record Measure(
        Verdict verdict,
        String code,
        Double value,
        String unit,
        Direction direction,
        int sampleSize,
        Double rSquared
) {

    public static Measure withoutConclusion(Verdict verdict, String unit, int sampleSize, Double rSquared) {
        return new Measure(verdict, null, null, unit, null, sampleSize, rSquared);
    }

    public boolean isConclusive() {
        return verdict == Verdict.CONCLUSIVE;
    }
}
