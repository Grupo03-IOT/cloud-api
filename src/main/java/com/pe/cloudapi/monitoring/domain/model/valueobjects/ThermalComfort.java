package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Confort térmico según ISO 7730 (modelo de Fanger). Se calcula en el Edge a
 * partir de temperatura y humedad más cuatro asunciones documentadas:
 * temperatura radiante media, velocidad del aire, tasa metabólica y
 * aislamiento de la ropa.
 *
 * <p>Sustituye al Heat Index, que no es válido en interiores: la regresión de
 * la NOAA solo aplica por encima de unos 26,7 &deg;C y 40% de humedad
 * relativa, y por debajo devuelve la temperatura seca sin más.
 *
 * @param pmv     Predicted Mean Vote, de -3 (frío) a +3 (calor)
 * @param ppd     Predicted Percentage of Dissatisfied; nunca baja del 5%
 * @param verdict etiqueta legible de la banda de PMV
 */
public record ThermalComfort(Float pmv, Float ppd, String verdict) {

    private static final float ASHRAE_55_ACCEPTABLE_PPD = 10f;

    public static ThermalComfort empty() {
        return new ThermalComfort(null, null, null);
    }

    public boolean isAcceptable() {
        return ppd != null && ppd < ASHRAE_55_ACCEPTABLE_PPD;
    }
}
