package com.pe.cloudapi.monitoring.domain.model.valueobjects;

import java.time.OffsetDateTime;

/** Optional model estimate kept separate from measured climate metrics. */
public record TemperatureForecast(
        Float probability,
        Integer horizonMin,
        Float temperatureLimitC,
        String modelVersion,
        OffsetDateTime generatedAt
) {
    public TemperatureForecast {
        if (probability == null || !Float.isFinite(probability) || probability < 0f || probability > 1f)
            throw new IllegalArgumentException("forecast.probability must be between 0 and 1");
        if (horizonMin == null || horizonMin < 1 || horizonMin > 1440)
            throw new IllegalArgumentException("forecast.horizon_min must be between 1 and 1440");
        if (temperatureLimitC == null || !Float.isFinite(temperatureLimitC))
            throw new IllegalArgumentException("forecast.temperature_limit_c must be finite");
        if (modelVersion == null || modelVersion.isBlank() || generatedAt == null)
            throw new IllegalArgumentException("forecast model_version and generated_at are required");
    }
}
