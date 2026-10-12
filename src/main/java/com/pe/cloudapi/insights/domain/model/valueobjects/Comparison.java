package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * El periodo frente al anterior. Un delta negativo es una mejora.
 *
 * @param pctOverDelta puntos porcentuales; nulo si no hay umbral
 * @param p90Delta     en dB
 */
public record Comparison(String previousPeriod, Double pctOverDelta, double p90Delta) {}
