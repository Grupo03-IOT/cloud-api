package com.pe.cloudapi.monitoring.domain.model.valueobjects;

import java.util.Optional;

/**
 * Los límites de un tipo de sala contra los que se contrasta un reporte de
 * incomodidad. Un límite {@code null} significa que no hay umbral para esa
 * métrica.
 *
 * <p>Hay discrepancia cuando la lectura está dentro del límite que corresponde
 * al motivo, límite incluido:
 * <ul>
 *   <li>{@code NOISE}: {@code laeq <= noiseWarnDba}</li>
 *   <li>{@code WARM}: {@code tempC <= tempUpperC}</li>
 *   <li>{@code COLD}: {@code tempC >= tempLowerC}</li>
 * </ul>
 *
 * @param noiseWarnDba valor de aviso de ruido, dBA
 * @param tempUpperC   límite superior de confort, °C
 * @param tempLowerC   límite inferior de confort, °C
 */
public record ComfortThresholds(Float noiseWarnDba, Float tempUpperC, Float tempLowerC) {

    public static ComfortThresholds none() {
        return new ComfortThresholds(null, null, null);
    }

    /**
     * @param category motivo del reporte
     * @param reading  lectura del momento del reporte; puede faltar
     * @return el motivo de la discrepancia; vacío si no la hay o si falta la
     *         lectura, la medida o el umbral para saberlo
     */
    public Optional<DiscrepancyReason> discrepancyWith(DiscomfortCategory category, ReadingAtReport reading) {
        if (reading == null) {
            return Optional.empty();
        }
        boolean within = switch (category) {
            case NOISE -> isAtMost(reading.laeq(), noiseWarnDba);
            case WARM -> isAtMost(reading.tempC(), tempUpperC);
            case COLD -> isAtMost(tempLowerC, reading.tempC());
        };
        if (!within) {
            return Optional.empty();
        }
        return Optional.of(category == DiscomfortCategory.NOISE
                ? DiscrepancyReason.NOISE_WITHIN_RANGE
                : DiscrepancyReason.TEMPERATURE_WITHIN_RANGE);
    }

    private static boolean isAtMost(Float value, Float limit) {
        return value != null && limit != null && value <= limit;
    }
}
