package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.results.RoomDiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ComfortThresholds;
import com.pe.cloudapi.monitoring.domain.ports.out.ComfortThresholdProvider;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Completa los reportes con el código de su sala y la discrepancia contra los
 * umbrales vigentes de su tipo. Busca cada sala y cada tipo una sola vez.
 */
@Component
@RequiredArgsConstructor
class RoomDiscomfortReports {

    private final RoomRepository rooms;
    private final ComfortThresholdProvider thresholds;

    List<RoomDiscomfortReport> of(List<DiscomfortReport> reports) {
        Map<UUID, Optional<Room>> roomsById = new HashMap<>();
        Map<UUID, ComfortThresholds> thresholdsByType = new HashMap<>();
        return reports.stream()
                .map(report -> {
                    Optional<Room> room = roomsById.computeIfAbsent(report.getRoomId(), rooms::findById);
                    ComfortThresholds limits = room.map(Room::getRoomTypeId)
                            .map(type -> thresholdsByType.computeIfAbsent(type, thresholds::comfortThresholdsOf))
                            .orElseGet(ComfortThresholds::none);
                    return new RoomDiscomfortReport(report,
                            room.map(Room::getCode).orElse(null),
                            limits.discrepancyWith(report.getCategory(), report.getReading()).orElse(null));
                })
                .toList();
    }

    RoomDiscomfortReport of(DiscomfortReport report) {
        return of(List.of(report)).getFirst();
    }
}
