package com.pe.cloudapi.insights.domain.model.valueobjects;

import java.time.OffsetDateTime;

/**
 * Minutos de ruido anómalo seguidos, contados como un solo suceso.
 *
 * @param from    primer minuto, inclusive
 * @param to      último minuto, inclusive
 * @param minutes de {@code from} a {@code to}, ambos incluidos
 */
public record NoiseEpisode(
        OffsetDateTime from,
        OffsetDateTime to,
        int minutes,
        float peakLaeq,
        OffsetDateTime peakAt
) {}
