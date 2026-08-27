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

    public String toCode() {
        return name().toLowerCase();
    }

    public static ThresholdMetric fromCode(String code) {
        return valueOf(code.toUpperCase());
    }
}
