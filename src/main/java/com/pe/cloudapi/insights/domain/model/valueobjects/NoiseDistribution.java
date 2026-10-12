package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Percentiles del nivel sonoro y, si hay umbral, cuánto tiempo se pasó de él.
 *
 * @param threshold   umbral de aviso de {@code laeq} del tipo de sala; nulo si no hay
 * @param minutesOver nulo si no hay umbral
 * @param pctOver     de 0 a 100; nulo si no hay umbral
 */
public record NoiseDistribution(
        double p50,
        double p90,
        double p95,
        int sampleSize,
        Float threshold,
        Integer minutesOver,
        Double pctOver
) {}
