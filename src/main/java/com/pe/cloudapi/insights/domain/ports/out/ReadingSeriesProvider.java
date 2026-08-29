package com.pe.cloudapi.insights.domain.ports.out;

import com.pe.cloudapi.insights.domain.model.valueobjects.ReadingPoint;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * De dónde salen las series que este contexto analiza.
 *
 * <p>Es un puerto <strong>de salida</strong>: {@code insights} declara qué
 * necesita, y otro se encarga de conseguirlo. Quien lo implementa traduce desde
 * {@code monitoring}, pero eso ocurre en la capa de infraestructura — el
 * dominio de este contexto no sabe que el otro existe.
 *
 * <p>Es una capa anticorrupción: si mañana las lecturas llegaran de otra parte,
 * cambiaría la implementación y nada más.
 */
public interface ReadingSeriesProvider {

    /**
     * @param roomId sala consultada
     * @param from   inicio del rango, inclusive
     * @param to     fin del rango, inclusive
     * @return los puntos ordenados de más antiguo a más reciente
     */
    List<ReadingPoint> seriesOf(UUID roomId, OffsetDateTime from, OffsetDateTime to);
}
