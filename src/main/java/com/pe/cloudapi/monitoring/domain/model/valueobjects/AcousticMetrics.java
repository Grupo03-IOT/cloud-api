package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Niveles de presión sonora de un periodo de agregación, con ponderación A y
 * expresados en dBA. Se calculan en el Edge siguiendo ISO 1996; el cloud no
 * los recalcula nunca.
 *
 * @param laeq nivel continuo equivalente de todo el periodo
 * @param l10  nivel superado el 10% del tiempo: picos intrusivos
 * @param l50  nivel mediano
 * @param l90  nivel superado el 90% del tiempo: ruido de fondo
 * @param lmax nivel instantáneo más alto observado
 * @param lmin nivel instantáneo más bajo observado
 */
public record AcousticMetrics(
        Float laeq,
        Float l10,
        Float l50,
        Float l90,
        Float lmax,
        Float lmin
) {
    public static AcousticMetrics empty() {
        return new AcousticMetrics(null, null, null, null, null, null);
    }

    public Float backgroundNoise() {
        return l90;
    }

    public Float intrusivePeaks() {
        return l10;
    }

    public boolean exceeds(Float limit) {
        return laeq != null && limit != null && laeq > limit;
    }
}
