package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListDiscomfortReportsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.DiscomfortSummary;
import com.pe.cloudapi.monitoring.application.internal.results.RoomDiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.ports.out.DiscomfortReportRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final RoomRepository rooms;

    @Override
    public List<RoomDiscomfortReport> execute(ListDiscomfortReportsQuery query) {
        return withRoomCodes(reports.search(query));
    }

    @Override
    public List<RoomDiscomfortReport> ofUser(UUID userId) {
        return withRoomCodes(reports.findByUserId(userId));
    }

    /**
     * Se cuenta en memoria sobre el rango pedido: el volumen de un local es de
     * decenas de reportes al día, y así el filtro de fechas es el mismo que el
     * del listado.
     */
    @Override
    public List<DiscomfortSummary> summarize(OffsetDateTime from, OffsetDateTime to) {
        Map<UUID, Map<DiscomfortCategory, Long>> counts = reports
                .search(ListDiscomfortReportsQuery.between(from, to)).stream()
                .collect(Collectors.groupingBy(DiscomfortReport::getRoomId, LinkedHashMap::new,
                        Collectors.groupingBy(DiscomfortReport::getCategory,
                                () -> new EnumMap<>(DiscomfortCategory.class), Collectors.counting())));
        Map<UUID, String> codes = new HashMap<>();
        return counts.entrySet().stream()
                .map(entry -> new DiscomfortSummary(
                        entry.getKey(),
                        codeOf(entry.getKey(), codes),
                        entry.getValue().getOrDefault(DiscomfortCategory.NOISE, 0L),
                        entry.getValue().getOrDefault(DiscomfortCategory.WARM, 0L),
                        entry.getValue().getOrDefault(DiscomfortCategory.COLD, 0L)))
                .sorted(Comparator.comparingLong(DiscomfortSummary::total).reversed()
                        .thenComparing(DiscomfortSummary::roomCode, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<RoomDiscomfortReport> withRoomCodes(List<DiscomfortReport> found) {
        Map<UUID, String> codes = new HashMap<>();
        return found.stream()
                .map(report -> new RoomDiscomfortReport(report, codeOf(report.getRoomId(), codes)))
                .toList();
    }

    private String codeOf(UUID roomId, Map<UUID, String> cache) {
        return cache.computeIfAbsent(roomId, id -> rooms.findById(id).map(Room::getCode).orElse(null));
    }
}
