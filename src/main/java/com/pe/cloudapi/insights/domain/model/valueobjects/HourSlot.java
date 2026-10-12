package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Una hora del día, en la zona del local, a lo largo del periodo.
 *
 * <p>Sin veredicto {@link Verdict#CONCLUSIVE} no vienen los valores.
 *
 * @param hour    de 0 a 23
 * @param avgLaeq promediado por energía, no aritméticamente
 */
public record HourSlot(
        int hour,
        Verdict verdict,
        int sampleSize,
        Double avgLaeq,
        Double p90Laeq,
        Double avgOccupiedPct
) {

    public static HourSlot withoutConclusion(int hour, Verdict verdict, int sampleSize) {
        return new HourSlot(hour, verdict, sampleSize, null, null, null);
    }
}
