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
    /**
     * Instancia sin datos, para minutos en los que no llegó nada acústico.
     *
     * @return métricas con todos los niveles nulos
     */
    public static AcousticMetrics empty() {
        return new AcousticMetrics(null, null, null, null, null, null);
    }

    /**
     * Ruido de fondo: lo que suena la sala cuando nadie hace nada llamativo.
     *
     * <p>Es el L90, el nivel superado el 90% del tiempo. Delata el aire
     * acondicionado, la calle o el zumbido de un equipo, que son las causas
     * que la administración puede corregir.
     *
     * @return el L90 del periodo
     */
    public Float backgroundNoise() {
        return l90;
    }

    /**
     * Nivel de los picos intrusivos: el L10, superado solo el 10% del tiempo.
     *
     * <p>Es lo que arruina una llamada aunque el promedio de la sala sea
     * aceptable.
     *
     * @return el L10 del periodo
     */
    public Float intrusivePeaks() {
        return l10;
    }

    /**
     * Compara el nivel continuo equivalente contra un límite.
     *
     * @param limit límite en dBA; {@code null} nunca se supera
     * @return {@code true} si el LAeq del periodo está por encima
     */
    public boolean exceeds(Float limit) {
        return laeq != null && limit != null && laeq > limit;
    }
}
