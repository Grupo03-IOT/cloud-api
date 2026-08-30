package com.pe.cloudapi.insights.domain.model.valueobjects;

import java.time.OffsetDateTime;

/**
 * Un punto de la serie temporal de una sala, en los términos que este contexto
 * necesita.
 *
 * <p>No es el agregado de {@code monitoring}: aquí solo entran las magnitudes
 * que se correlacionan o se ajustan. Esa reducción es lo que permite que
 * {@code insights} no dependa del modelo del otro contexto.
 *
 * @param ts          minuto que describe el punto
 * @param laeq        nivel sonoro equivalente en dBA
 * @param backgroundNoise nivel superado el 90% del tiempo
 * @param tempC       temperatura del aire
 * @param ppd         porcentaje de insatisfechos
 * @param occupiedPct porcentaje del minuto con presencia
 */
public record ReadingPoint(
        OffsetDateTime ts,
        Float laeq,
        Float backgroundNoise,
        Float tempC,
        Float ppd,
        Float occupiedPct
) {}
