package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Magnitud sobre la que se puede configurar un
 * {@link com.pe.cloudapi.monitoring.domain.model.entities.Threshold}.
 *
 * <p>Se persiste en minúsculas; convertir con {@link #toCode()} y
 * {@link #fromCode(String)}.
 */
public enum ThresholdMetric {
    LAEQ,
    L10,
    PPD,
    OCCUPIED_PCT,
    TEMP_C;

    /**
     * Representación persistida, en minúsculas.
     *
     * @return el nombre de la constante en minúsculas
     */
    public String toCode() {
        return name().toLowerCase();
    }

    /**
     * Reconstruye la métrica desde su representación persistida.
     *
     * <p>Un valor desconocido falla a propósito: un umbral sobre una métrica
     * que el sistema no sabe evaluar es un error de configuración que conviene
     * ver de inmediato.
     *
     * @param code texto guardado
     * @return la métrica correspondiente
     * @throws IllegalArgumentException si el texto no corresponde a ninguna
     */
    public static ThresholdMetric fromCode(String code) {
        return valueOf(code.toUpperCase());
    }
}
