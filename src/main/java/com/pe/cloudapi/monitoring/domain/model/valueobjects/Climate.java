package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Lecturas ambientales tomadas por el sensor SHT31.
 *
 * @param tempC temperatura del aire en grados Celsius
 * @param rhPct humedad relativa en porcentaje
 */
public record Climate(Float tempC, Float rhPct) {

    /**
     * Instancia sin datos, para minutos en los que no llegó lectura ambiental.
     *
     * @return clima con temperatura y humedad nulas
     */
    public static Climate empty() {
        return new Climate(null, null);
    }
}
