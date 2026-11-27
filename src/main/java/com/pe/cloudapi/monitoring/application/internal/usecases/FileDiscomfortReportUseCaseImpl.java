package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.FileDiscomfortReportUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.RoomDiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.commands.FileDiscomfortReportCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ReadingAtReport;
import com.pe.cloudapi.monitoring.domain.ports.out.DiscomfortReportRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Registrar un reporte de incomodidad con la lectura del momento (US27).
 *
 * <p>La última lectura se busca igual que {@code readings/latest}; la decisión
 * de si es reciente y el límite de frecuencia los toma el agregado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileDiscomfortReportUseCaseImpl implements FileDiscomfortReportUseCase {

    private final RoomRepository rooms;
    private final RoomReadingRepository readings;
    private final DiscomfortReportRepository reports;
    private final RoomDiscomfortReports roomReports;

    @Override
    @Transactional
    public RoomDiscomfortReport execute(FileDiscomfortReportCommand command) {
        Room room = rooms.findById(command.roomId())
                .orElseThrow(() -> MonitoringError.ROOM_NOT_FOUND.with(command.roomId()));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        DiscomfortReport report = new DiscomfortReport(
                command,
                reports.findLatest(command.userId(), room.getId(), command.category()),
                readings.findLatest(room.getId()).map(ReadingAtReport::of),
                now);
        DiscomfortReport saved = reports.save(report);
        log.info("discomfort.reported id={} room={} category={} withReading={}",
                saved.getId(), room.getCode(), saved.getCategory(), saved.getReading() != null);
        return roomReports.of(saved);
    }
}
