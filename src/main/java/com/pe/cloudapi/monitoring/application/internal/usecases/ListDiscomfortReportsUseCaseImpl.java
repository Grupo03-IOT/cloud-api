package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListDiscomfortReportsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.DiscomfortSummary;
import com.pe.cloudapi.monitoring.application.internal.results.RoomDiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.ports.out.DiscomfortReportRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Consultar los reportes de incomodidad (US28) y los propios del miembro.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListDiscomfortReportsUseCaseImpl implements ListDiscomfortReportsUseCase {

    private final DiscomfortReportRepository reports;
    private final RoomDiscomfortReports roomReports;

    /**
     * La discrepancia no se guarda: se calcula con los umbrales vigentes, así
     * que su filtro se aplica después de buscar.
     */
    @Override
    public List<RoomDiscomfortReport> execute(ListDiscomfortReportsQuery query) {
        List<RoomDiscomfortReport> found = roomReports.of(reports.search(query));
        if (query.discrepancy() == null) {
            return found;
        }
        return found.stream()
                .filter(report -> report.hasDiscrepancy() == query.discrepancy())
                .toList();
    }

    @Override
    public List<RoomDiscomfortReport> ofUser(UUID userId) {
        return roomReports.of(reports.findByUserId(userId));
    }

    /**
     * Se cuenta en memoria sobre el rango pedido: el volumen de un local es de
     * decenas de reportes al día, y así el filtro de fechas es el mismo que el
     * del listado.
     */
    @Override
    public List<DiscomfortSummary> summarize(OffsetDateTime from, OffsetDateTime to) {
        return roomReports.of(reports.search(ListDiscomfortReportsQuery.between(from, to))).stream()
                .collect(Collectors.groupingBy(result -> result.report().getRoomId(),
                        LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(ListDiscomfortReportsUseCaseImpl::summaryOf)
                .sorted(Comparator.comparingLong(DiscomfortSummary::total).reversed()
                        .thenComparing(DiscomfortSummary::roomCode, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private static DiscomfortSummary summaryOf(List<RoomDiscomfortReport> ofRoom) {
        RoomDiscomfortReport first = ofRoom.getFirst();
        return new DiscomfortSummary(
                first.report().getRoomId(),
                first.roomCode(),
                countOf(ofRoom, DiscomfortCategory.NOISE),
                countOf(ofRoom, DiscomfortCategory.WARM),
                countOf(ofRoom, DiscomfortCategory.COLD),
                ofRoom.stream().filter(RoomDiscomfortReport::hasDiscrepancy).count());
    }

    private static long countOf(List<RoomDiscomfortReport> ofRoom, DiscomfortCategory category) {
        return ofRoom.stream().filter(result -> result.report().getCategory() == category).count();
    }
}
