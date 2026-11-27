package com.pe.cloudapi.monitoring.domain.ports.out;

import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de {@link DiscomfortReport}.
 */
public interface DiscomfortReportRepository {

    DiscomfortReport save(DiscomfortReport report);

    /**
     * El último reporte de un usuario con ese motivo en esa sala: es lo que
     * aplica el límite de frecuencia.
     */
    Optional<DiscomfortReport> findLatest(UUID userId, UUID roomId, DiscomfortCategory category);

    /** Los que cumplen los filtros, del más reciente al más antiguo. */
    List<DiscomfortReport> search(ListDiscomfortReportsQuery query);

    /** Los de un usuario, del más reciente al más antiguo. */
    List<DiscomfortReport> findByUserId(UUID userId);
}
