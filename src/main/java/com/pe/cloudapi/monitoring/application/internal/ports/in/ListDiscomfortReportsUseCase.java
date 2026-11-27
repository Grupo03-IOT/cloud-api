package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.DiscomfortSummary;
import com.pe.cloudapi.monitoring.application.internal.results.RoomDiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: consultar reportes de incomodidad.
 */
public interface ListDiscomfortReportsUseCase {

    /** Todos los que cumplen los filtros, del más reciente al más antiguo. */
    List<RoomDiscomfortReport> execute(ListDiscomfortReportsQuery query);

    /** Los del usuario autenticado, del más reciente al más antiguo. */
    List<RoomDiscomfortReport> ofUser(UUID userId);

    /** Cuenta por sala y motivo, de la sala con más reportes a la que menos. */
    List<DiscomfortSummary> summarize(OffsetDateTime from, OffsetDateTime to);
}
