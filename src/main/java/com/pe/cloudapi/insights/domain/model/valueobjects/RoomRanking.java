package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Una sala en la comparativa de su local.
 *
 * @param noiseDistribution nula si la sala no tiene lecturas de ruido en el periodo
 */
public record RoomRanking(AnalyzedRoom room, DataQuality dataQuality, NoiseDistribution noiseDistribution) {}
