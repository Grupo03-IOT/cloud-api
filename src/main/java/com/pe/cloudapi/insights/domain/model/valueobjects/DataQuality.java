package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Cuánto del periodo tiene lecturas.
 *
 * @param expectedMinutes minutos del periodo completo, aunque todavía no haya terminado
 * @param receivedMinutes minutos con lectura
 * @param coveragePct     de 0 a 100, con un decimal
 */
public record DataQuality(long expectedMinutes, int receivedMinutes, double coveragePct) {

    public static DataQuality of(long expectedMinutes, int receivedMinutes) {
        double coverage = expectedMinutes == 0 ? 0 : receivedMinutes * 100.0 / expectedMinutes;
        return new DataQuality(expectedMinutes, receivedMinutes, Math.round(coverage * 10) / 10.0);
    }
}
